# StableEconomy

**StableEconomy** is a Paper plugin for Minecraft that provides a flexible in-game economy system with configurable shops, currencies, and prices.

## Features

- Multi-currency support
- Configurable economy commands for balance, payment, leaderboard, and administration
- Configurable shops
- Customizable messages

## Requirements

- Java 21 or newer
- Paper server (1.21.5+) 

## Configuration

Configuration files are in `src/main/resources` and will be generated in your server’s `plugins/StableEconomy` folder:

- `database.yml` – Database settings (type, credentials)
- `prices.yml` – Default item prices
- `shops.yml` – Global shop configuration
- `shops/` – Folder for individual shop category files
- `currencies/` – Folder for defining custom currencies (each subfolder includes `currency.yml` and `locale.yml`)

## Contributing

Contributions are welcome! Please fork the repository, make your changes, and open a pull request. Ensure code follows the existing style

## License

This project is licensed under the GNU General Public License v3.0.
See the [LICENSE](LICENSE) file for full terms and conditions.

## TODO

- [ ] Implement a reload command for configurations
- [ ] Implement better configuration error handling (providing the specific line where the error takes place)
- [ ] Implement NBT support for AdvancedPricedItems
- [ ] Implement support for backups & restores
- [ ] Implement customizable shop messages (may require a file structure like the currency system)
- [ ] Implement the ability to customize all plugin messages
- [ ] Implement a configurable sell command with the ability to sell specific items, all items, hand, and within a gui
- [ ] Implement full control of currency command-related messages