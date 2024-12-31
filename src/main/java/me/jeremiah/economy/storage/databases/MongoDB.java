package me.jeremiah.economy.storage.databases;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import me.jeremiah.economy.config.BasicConfig;
import me.jeremiah.economy.storage.BalanceEntry;
import me.jeremiah.economy.storage.DatabaseInfo;
import me.jeremiah.economy.storage.PlayerAccount;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class MongoDB extends Database {

  MongoDB(@NotNull BasicConfig config) {
    super(config);

    DatabaseInfo databaseInfo = config.getDatabaseInfo();

    MongoClientSettings settings = MongoClientSettings.builder()
      .applyConnectionString(new ConnectionString("mongodb://%s/%s".formatted(databaseInfo.getUrl(), databaseInfo.getName())))
      .uuidRepresentation(UuidRepresentation.STANDARD)
      .credential(MongoCredential.createCredential(databaseInfo.getUsername(), databaseInfo.getName(), databaseInfo.getPassword().toCharArray()))
      .build();

    client = MongoClients.create(settings);
    database = client.getDatabase(databaseInfo.getName());
    accounts = database.getCollection("accounts");
    accounts.createIndex(new Document("uniqueId", 1), new IndexOptions().unique(true));
  }

  private final MongoClient client;
  private final MongoDatabase database;
  private final MongoCollection<Document> accounts;

  @Override
  void load() {
    for (Document document : accounts.find()) {
      UUID uniqueId = document.get("uniqueId", UUID.class);
      Document balanceDocument = document.get("balances", Document.class);
      HashMap<String, BalanceEntry> balances = new HashMap<>();
      for (HashMap.Entry<String, Object> entry : balanceDocument.entrySet())
        balances.put(entry.getKey(), new BalanceEntry(entry.getKey(), (Double) entry.getValue()));
      add(new PlayerAccount(uniqueId, document.getString("username"), balances));
    }
  }

  @Override
  void save() {
    List<UpdateOneModel<Document>> writeModels = new ArrayList<>();

    for (PlayerAccount account : entries) {
      Document document = new Document("uniqueId", account.getUniqueId());

      if (account.isDirty())
        document.append("username", account.getUsername());

      Map<String, Double> balances = new HashMap<>();
      for (BalanceEntry balanceEntry : account.getBalanceEntries())
        if (balanceEntry.isDirty())
          balances.put(balanceEntry.getCurrency(), balanceEntry.getBalance());

      document.append("balances", new Document(balances));

      writeModels.add(new UpdateOneModel<>(
        new Document("uniqueId", account.getUniqueId()),
        new Document("$set", document),
        new UpdateOptions().upsert(true)
      ));
    }

    accounts.bulkWrite(writeModels);
  }

}
