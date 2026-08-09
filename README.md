# 🎮 ModelFramework

A lightweight and extensible Paper plugin framework for connecting **BetterModel** models to interactive world objects.

Transform any BetterModel model into a fully interactive object — crates, furniture, machines, doors, decorations, or anything you can imagine.

---

## ⚠️ Premium Plugin

> **This repository contains a LIMITED public version of ModelFramework.**
>
> The complete version with full source code, all features, and future updates is available exclusively on Spigot.
>
> 🔗 **Get the Premium version:** [ModelFramework on Spigot](https://www.spigotmc.org/resources/XXXXX/)
>
> **What's different in Premium?**
> - Complete source code and all core classes
> - Full feature set
> - Priority support and updates
> - Commercial server usage license

---

## 🎬 Demo

![ModelFramework Demo](assets/demo.gif)

---

## ✨ What ModelFramework Does

- **YAML-Based Object Definitions** — Define objects entirely through configuration files
- **BetterModel Integration** — Seamless connection to BetterModel 3.3.0+ API
- **Animation System** — Trigger animations on interaction with full control
- **Smart Interaction** — RIGHT_CLICK, LEFT_CLICK, SHIFT variants with item requirements
- **Item Provider System** — Oraxen, ItemsAdder, and Vanilla item support
- **Hand/Inventory Check** — Configure whether required items must be held or just in inventory
- **State Machine** — Simple but powerful state system for objects
- **Smart Rotation** — Models automatically face the player direction
- **Scale & Offset** — Configure model scale and position per object
- **Category GUI** — Two-level GUI for browsing and selecting objects
- **Instance Persistence** — Placed objects survive server restarts
- **Security** — OP-only placement with automatic item cleanup
- **Action System** — Sounds, messages, and commands on triggers
- **Cooldown System** — Per-player, per-instance cooldowns
- **Performance** — Fully event-driven, no tick loops

> **ModelFramework is not a crate plugin.**
> It's a generic framework. Crates are just one example.

---

## 📋 Requirements

| Dependency | Version | Required |
|---|---|---|
| **Paper** | 1.21.1+ | ✅ Yes |
| **Java** | 25+ | ✅ Yes |
| **BetterModel** | 3.3.0+ | ✅ Yes |
| **Oraxen** | 1.207.0+ | ❌ Optional |
| **ItemsAdder** | — | ❌ Optional |
| **PlaceholderAPI** | 2.11.6+ | ❌ Optional |

---

## 📁 File Structure
plugins/
└── ModelFramework/
├── config.yml
├── objects/
│ ├── crates.yml
│ ├── furniture.yml
│ └── machines.yml
└── data/
└── instances.yml

text


---

## ⚙️ Configuration Example

### `config.yml`

```yaml
debug: false

gui:
  title: "&8Model Categories"
  category-title: "&8Objects: %category%"
  size: 54

defaults:
  cooldown: 3

storage:
  auto-save: 5
```

## 📦 Object Definition Example

### `legendary_crate`

```yaml
legendary_crate:
  display:
    name: "&6✦ Legendary Crate"
    material: CHEST
    lore:
      - "&7Right-click to place"
    slot: 16

  model:
    id: legendary_crate
    scale: 1.0
    offset-x: 0.0
    offset-y: 0.0
    offset-z: 0.0
    rotation-offset: 180

  placement:
    type: BLOCK
    base-material: BARRIER

  state:
    initial: DEFAULT

  animations:
    open:
      animation: animation.common_crate.open
      loop: false
      trigger:
        type: RIGHT_CLICK
      item:
        provider: oraxen
        id: "Legendary Key"
        consume: true
        check: HAND
      cooldown:
        time: 3
      actions:
        - type: SOUND
          sound: entity.firework_rocket.blast
          volume: 1.0
          pitch: 0.6

    idle:
      animation: animation.common_crate.idle
      loop: false
      trigger:
        type: RIGHT_CLICK
      item:
        required: false
      cooldown:
        time: 1
      actions:
        - type: SOUND
          sound: entity.villager.no
          volume: 0.5
          pitch: 1.2
```

## 🎯 How It Works

YAML Object Definition
    ↓
GUI → Select Object → Receive Item
    ↓
Place Item in World (OP only)
    ↓
BetterModel spawns on ItemDisplay
    ↓
Player interacts → Animation + Actions


🎨 Trigger Types
Trigger	Description
RIGHT_CLICK	Right-click on object
LEFT_CLICK	Left-click on object
SHIFT_RIGHT_CLICK	Shift + right-click
SHIFT_LEFT_CLICK	Shift + left-click
NONE	No trigger
🎵 Action Types
Action	Description
SOUND	Play a Minecraft sound
MESSAGE	Send a message to the player
COMMAND	Execute a command (CONSOLE or PLAYER)
🔑 Item Check Modes
Mode	Description
HAND	Item must be in main hand or off hand
INVENTORY	Item can be anywhere in inventory
🔧 Commands
Command	Description
/model gui	Open category selector GUI
/model give <object>	Get an object placement item
/model reload	Reload configuration
/model inspect	Inspect target block
/model remove	Remove target instance
/model list	List all objects
/model debug	Toggle debug mode
Aliases: /mf, /modelfw

📊 Use Cases
Use Case	How
Crates	Model + open animation + key requirement
Doors	Model + open/close animations + state toggle
Furniture	Model + scale/offset
Machines	Model + start/stop animations + item fuel
Decorations	Model + idle animation loop
NPCs	Model + click response + command actions
Shops	Model + click trigger + command
🔒 Security
Only OP players can place framework objects
Non-OP players who obtain framework items will have them automatically removed
📄 License
This project is provided under the ModelFramework License.
See LICENSE for full details.

Summary:

✅ View source code
✅ Use on personal/private servers
✅ Modify privately
❌ Redistribute
❌ Resell or sublicense
❌ Republish
❌ Use in commercial projects without permission
❌ Claim as your own work
The complete Premium version is proprietary.

🙏 Credits
BetterModel — Model rendering engine
Paper — Server platform
Oraxen — Custom item support (optional)
📞 Support
Premium support: Available through Spigot
Issues: GitHub Issues
ModelFramework — Turn any BetterModel model into an interactive world object.

text


---

# B) LICENSE
ModelFramework License

Copyright (c) 2025 Abolfazl Delkhosh. All rights reserved.

This software and associated documentation files (the "Software") are
provided for limited use under the following terms:

PERMITTED:

You may view and read the source code in this repository.

You may use the public version of the Software on personal or
private Minecraft servers that you own or operate.

You may modify the Software privately for your own personal or
private server use. Modified versions must not be distributed,
published, or shared in any form.

PROHIBITED:

You may NOT redistribute the Software, in whole or in part,
in source code or compiled form, to any third party.

You may NOT republish, reupload, mirror, or host the Software
or any substantial portion of it on any platform, repository,
website, or file-sharing service.

You may NOT sell, resell, sublicense, lease, rent, or otherwise
commercially transfer the Software or any rights to it.

You may NOT upload compiled versions (JAR files or similar) of
the Software to any third-party platform, marketplace, or
distribution channel without explicit written permission from
the copyright holder.

You may NOT use substantial portions of the source code in
another commercial project, plugin, mod, or software product
without explicit written permission from the copyright holder.

You may NOT claim authorship or ownership of the Software or
any portion of it. Proper attribution to the original author
must be maintained in all permitted uses.

PREMIUM VERSION:

The complete Premium version of ModelFramework, including all
features, full source code, and compiled distributions available
through Spigot or other authorized channels, is proprietary
software owned exclusively by Abolfazl Delkhosh.

The Premium version is subject to its own separate license terms
provided at the point of purchase.

DISCLAIMER:

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT.

IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

For permissions beyond those granted here, contact:
GitHub: https://github.com/dlkhwshabwlfdl-web
