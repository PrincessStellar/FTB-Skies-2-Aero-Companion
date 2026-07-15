# FTB Skies 2: Aero Companion

The companion mod for the [FTB Skies 2: Aero](https://www.feed-the-beast.com/) modpack. It bundles modpack-specific fixes, content additions, and mixins that exist purely to make the Aero pack play the way we want it to.

## About `'Companion'` mods

FTB `Skies 2: Aero` Companion is a custom tailored, bespoke mod designed to work hand-in-hand with FTB Skies 2: Aero. Although it is visible source and released to CurseForge, we **do not** recommend using it inside other modpacks.

Please feel free to contribute to the project but **always** open an issue first before opening feature specific pull requests.

Companion mods are provided `as is`. If you opt to use this mod inside another modpack, we **will not** provide support and any issues opened regarding problems due to use in another modpack will be closed.

## AeroScoop Recipes

AeroScoop recipes are datapack-driven JSON files under `data/<namespace>/recipe/aeroscoop/`. Each recipe picks a mesh ingredient, an optional biome filter, and a list of weighted result drops.

### Fields

- `mesh` — vanilla `Ingredient` (item id or `{ "tag": "..." }`) the AeroScoop must contain.
- `biome` — biome filter string. Use `"ANY"` (or omit) for any biome, a biome id like `"minecraft:plains"`, or a tag prefixed with `#` like `"#minecraft:is_overworld"`.
- `results` — list of drops. Each entry has:
  - `item` — output item id.
  - `count` — stack size (optional, default `1`).
  - `components` — vanilla data-component patch (optional). Same syntax as anywhere else in 1.21 recipes.
  - `chance` — roll weight. Values `< 1` are a probability; values `≥ 1` are a guaranteed multiplier plus a fractional bonus chance (e.g. `2.25` = 2 guaranteed, 25% chance of a third).

### Simple recipe (no components)

```json
{
  "type": "ftbskies2aerocompanion:aeroscoop",
  "mesh": { "item": "ftb:diamond_mesh" },
  "biome": "ANY",
  "results": [
    {
      "item": "ftbstuff:dust",
      "count": 1,
      "chance": 1.0
    }
  ]
}
```

### Recipe with data components

```json
{
  "type": "ftbskies2aerocompanion:aeroscoop",
  "mesh": { "item": "ftb:diamond_mesh" },
  "biome": "#minecraft:is_overworld",
  "results": [
    {
      "item": "minecraft:diamond_sword",
      "count": 1,
      "components": {
        "minecraft:custom_name": "'{\"text\":\"Cloud Cutter\",\"color\":\"aqua\"}'",
        "minecraft:enchantments": {
          "levels": {
            "minecraft:sharpness": 3,
            "minecraft:unbreaking": 2
          }
        },
        "minecraft:lore": [
          "'{\"text\":\"Pulled from the wind itself.\",\"italic\":true,\"color\":\"gray\"}'"
        ]
      },
      "chance": 0.05
    },
    {
      "item": "minecraft:diamond",
      "count": 2,
      "chance": 0.5
    }
  ]
}
```

## Support

- For **Modpack** issues, please go here: https://go.ftb.team/support-modpack
- For **Mod** issues, please go here: https://go.ftb.team/support-mod-issues
- Just got a question? Check out our Discord: https://go.ftb.team/discord

## Licence

All Rights Reserved to Feed The Beast Ltd. Source code is `visible source`, please see our [LICENSE.md](/LICENSE.md) for more information. Any Pull Requests made to this mod must have the CLA (Contributor Licence Agreement) signed and agreed to before the request will be considered.

## Keep up to date

[![FTB Socials](https://cdn.feed-the-beast.com/assets/socials/icons/socials-scaled-cf.webp?ref=curseforge)](https://feed-the-beast.com/links)
