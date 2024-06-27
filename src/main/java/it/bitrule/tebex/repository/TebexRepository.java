package it.bitrule.tebex.repository;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.miwiklark.common.repository.Repository;
import it.bitrule.tebex.object.model.TebexIdTransaction;
import it.bitrule.tebex.object.model.TebexTransaction;
import it.bitrule.tebex.object.tebex.Payment;
import it.bitrule.tebex.object.tebex.PaymentsInfo;
import it.bitrule.tebex.service.PaymentsService;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.UUID;

public final class TebexRepository {

    public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-ddhh:mm:ss");
    private @Nullable PaymentsService paymentsService = null;

    public void init(@NonNull String tebexSecret) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://plugin.buycraft.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder()
                        .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                                .addHeader("X-Tebex-Secret", tebexSecret)
                                .build()
                        ))

                        .build()
                )
                .build();

        this.paymentsService = retrofit.create(PaymentsService.class);
    }

    public @Nullable Payment lookup(@NonNull String transactionId) {
        if (this.paymentsService == null) {
            throw new RuntimeException("Payments service not initialized");
        }

        try {
            Response<Payment> response = this.paymentsService.lookup(transactionId).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to fetch payment: " + response.errorBody().string() + " (" + response.code() + ") / " + response.message());
            }

            Payment payment = response.body();
            if (payment == null) {
                throw new RuntimeException("Failed to fetch payment: " + response.errorBody().string() + " (" + response.code() + ") / " + response.message());
            }

            return payment;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch payment: " + e.getMessage(), e);
        }
    }

    public void adapt(@NonNull LabymodRepository labymodRepository, @NonNull String dbName) throws Exception {
        if (this.paymentsService == null) {
            throw new RuntimeException("Payments service not initialized");
        }

        // This repository is only for who already redeemed the package
        Miwiklark.addRepository(
                TebexIdTransaction.class,
                dbName,
                "id"
        );

        Repository<TebexTransaction> premiumRepository = Miwiklark.addRepository(
                TebexTransaction.class,
                dbName,
                "premium"
        );

        int page = 1;
        while (page != -1) {
            Response<PaymentsInfo> response = this.paymentsService.retrieve(page).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to fetch payments: " + response.errorBody().string() + " (" + response.code() + ") / " + response.message());
            }

            PaymentsInfo paymentsInfo = response.body();
            if (paymentsInfo == null) {
                throw new RuntimeException("Failed to fetch payments: " + response.errorBody().string() + " (" + response.code() + ") / " + response.message());
            }

            if (paymentsInfo.getPayments().isEmpty()) {
                System.out.println("No payments found");

                break;
            }

            for (Payment payment : paymentsInfo.getPayments()) {
                if (!payment.getStatus().equalsIgnoreCase("complete")) continue;

                if (payment.getPlayer().getUniqueId().startsWith("0000000000000000000000")) continue;

                String transactionId = payment.getId();
                if (premiumRepository.findOne(transactionId).isPresent()) continue;

                String username = payment.getPlayer().getName();
                System.out.println("Processing transaction " + transactionId + " for " + username + " with packages " + payment.getPackages());

                UUID uniqueId = labymodRepository.validate(username, payment.getDate());
                if (uniqueId == null) {
                    System.out.println("Failed to fetch previous accounts for " + username);

                    continue;
                }

                System.out.println("Found previous account for " + username + " with UUID " + uniqueId);

                premiumRepository.save(new TebexTransaction(transactionId, uniqueId.toString(), payment.getPackages()));
            }

            page++;
        }
    }
}