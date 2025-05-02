package org.stablerpg.stableeconomy.data.databases;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.jetbrains.annotations.NotNull;
import org.stablerpg.stableeconomy.EconomyPlatform;
import org.stablerpg.stableeconomy.data.BalanceEntry;
import org.stablerpg.stableeconomy.data.PlayerAccount;
import org.stablerpg.stableeconomy.data.util.DatabaseInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MongoDB extends Database {

  private final MongoClient client;
  private final MongoCollection<Document> accounts;

  public MongoDB(@NotNull EconomyPlatform platform) {
    super(platform);

    DatabaseInfo databaseInfo = getConfig().getDatabaseInfo();

    MongoClientSettings settings = MongoClientSettings.builder()
      .applyConnectionString(new ConnectionString("mongodb://%s/%s".formatted(databaseInfo.getUrl(), databaseInfo.getName())))
      .uuidRepresentation(UuidRepresentation.STANDARD)
      .credential(MongoCredential.createCredential(databaseInfo.getUsername(), databaseInfo.getName(), databaseInfo.getPassword().toCharArray()))
      .build();

    client = MongoClients.create(settings);
    accounts = client.getDatabase(databaseInfo.getName()).getCollection("accounts");
    accounts.createIndex(new Document("uniqueId", 1), new IndexOptions().unique(true));

    setup();
  }

  protected int lookupEntryCount() {
    return (int) accounts.countDocuments();
  }

  @Override
  protected void load() {
    for (Document document : accounts.find()) {
      UUID uniqueId = document.get("uniqueId", UUID.class);
      String username = document.getString("username");
      Document balanceDocument = document.get("balances", Document.class);
      HashMap<String, BalanceEntry> balances = new HashMap<>();
      for (Map.Entry<String, Object> entry : balanceDocument.entrySet())
        balances.put(entry.getKey(), new BalanceEntry(entry.getKey(), (Double) entry.getValue()));

      add(new PlayerAccount(getPlatform(), uniqueId, username, balances));
    }
  }

  @Override
  protected void save() {
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

    accounts.bulkWrite(writeModels, new BulkWriteOptions().ordered(false));
  }

  @Override
  public void close() {
    super.close();
    client.close();
  }

}
