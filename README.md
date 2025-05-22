# StableEconomy

**StableEconomy** is a Paper plugin for Minecraft that provides a flexible in-game economy system with configurable shops, currencies, and prices.

## Features

- Multiple currencies support
- Configurable shops
- Configurable economy commands for balance, payment, leaderboard, and administration
- Customizable messages

## Requirements

- Java 21 or newer
- Paper server (1.21.5+) 

## Configuration

Configuration files are in `src/main/resources` and will be generated in your server’s `plugins/StableEconomy` folder:

- `database.yml` – Database settings (type, credentials)
- `messages.yml` – All chat and console messages
- `prices.yml` – Default item prices
- `shops.yml` – Global shop configuration
- `shops/` – Folder for individual shop category files
- `currencies/` – Folder for defining custom currencies (each subfolder includes `currency.yml` and optional `locale.yml`)

## Contributing

Contributions are welcome! Please fork the repository, make your changes, and open a pull request. Ensure code follows the existing style and includes tests where appropriate.

## License

This project is licensed under the GNU General Public License v3.0.
See the [LICENSE](LICENSE) file for full terms and conditions.

## TODO

- [ ] Implement a reload command for configurations
- [ ] Implement better configuration error handling (providing the specific line the error takes place)
- [ ] Implement NBT support for AdvancedPricedItems
- [ ] Implement support for backups & restores