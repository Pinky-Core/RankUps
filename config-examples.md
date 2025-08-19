# Ejemplos de Configuración - VanguardRankUps

## Integración con LuckPerms

### Ejemplo Básico con LuckPerms
```yaml
settings:
  luckperms:
    enabled: true
    remove_previous_group: true
    respect_luckperms_prefix: true

ranks:
  "1":
    name: "&7Novato"
    display_name: "&7[Novato]"
    luckperms_group: "novato"
    requirements:
      mob_kills:
        ZOMBIE: 50
      block_breaks:
        STONE: 100
      playtime_minutes: 60
    rewards:
      commands:
        - "broadcast &a¡%player% ha alcanzado el rango Novato!"
      items:
        - "IRON_SWORD:1"
      experience: 100
      money: 500
    cost: 0

  "2":
    name: "&aAprendiz"
    display_name: "&a[Aprendiz]"
    luckperms_group: "aprendiz"
    requirements:
      mob_kills:
        ZOMBIE: 100
        SKELETON: 50
      block_breaks:
        STONE: 200
        IRON_ORE: 20
      playtime_minutes: 180
    rewards:
      commands:
        - "broadcast &a¡%player% ha alcanzado el rango Aprendiz!"
        - "eco give %player% 2000"
      items:
        - "DIAMOND_SWORD:1"
        - "IRON_ARMOR:1"
      experience: 250
      money: 1000
    cost: 0
```

### Ejemplo con Prefixes Personalizados
Si quieres que el plugin use sus propios prefixes en lugar de los de LuckPerms:

```yaml
settings:
  luckperms:
    enabled: true
    remove_previous_group: true
    respect_luckperms_prefix: false  # El plugin usará sus propios prefixes

ranks:
  "1":
    name: "&7Novato"
    display_name: "&7[Novato] &r"  # Este prefix se aplicará al jugador
    luckperms_group: "novato"
    # ... resto de configuración
```

## Ejemplo 1: Sistema de Rangos Básico

```yaml
ranks:
  "1":
    name: "&7Novato"
    display_name: "&7[Novato]"
    requirements:
      mob_kills:
        ZOMBIE: 25
        SKELETON: 15
      block_breaks:
        STONE: 50
        COAL_ORE: 10
      playtime_minutes: 30
    rewards:
      commands:
        - "eco give %player% 500"
        - "broadcast &a¡%player% ha alcanzado el rango Novato!"
      items:
        - "IRON_SWORD:1"
        - "IRON_HELMET:1"
      experience: 50
      money: 250
    cost: 0

  "2":
    name: "&aAprendiz"
    display_name: "&a[Aprendiz]"
    requirements:
      mob_kills:
        ZOMBIE: 75
        SKELETON: 45
        CREEPER: 10
      block_breaks:
        STONE: 150
        IRON_ORE: 25
        COAL_ORE: 30
      playtime_minutes: 120
    rewards:
      commands:
        - "eco give %player% 1500"
        - "broadcast &a¡%player% ha alcanzado el rango Aprendiz!"
      items:
        - "DIAMOND_SWORD:1"
        - "DIAMOND_HELMET:1"
      experience: 150
      money: 750
    cost: 500
```

## Ejemplo 2: Sistema de Rangos Avanzado con Múltiples Tareas

```yaml
ranks:
  "1":
    name: "&7Iniciado"
    display_name: "&7[Iniciado]"
    requirements:
      mob_kills:
        ZOMBIE: 50
        SKELETON: 30
        SPIDER: 20
      block_breaks:
        STONE: 100
        COAL_ORE: 20
        DIRT: 200
      playtime_minutes: 60
      fishing:
        COD: 10
        SALMON: 5
      farming:
        WHEAT: 50
        CARROT: 25
    rewards:
      commands:
        - "eco give %player% 1000"
        - "broadcast &a¡%player% ha alcanzado el rango Iniciado!"
        - "lp user %player% parent set iniciado"
      items:
        - "DIAMOND_SWORD:1"
        - "IRON_ARMOR:1"
        - "BREAD:64"
      experience: 100
      money: 500
    cost: 0

  "2":
    name: "&aExplorador"
    display_name: "&a[Explorador]"
    requirements:
      mob_kills:
        ZOMBIE: 150
        SKELETON: 100
        CREEPER: 25
        ENDERMAN: 5
      block_breaks:
        STONE: 300
        IRON_ORE: 50
        GOLD_ORE: 10
        DIAMOND_ORE: 2
      playtime_minutes: 240
      fishing:
        COD: 30
        SALMON: 20
        TROPICAL_FISH: 5
      farming:
        WHEAT: 150
        CARROT: 100
        POTATO: 100
        BEETROOT: 50
    rewards:
      commands:
        - "eco give %player% 3000"
        - "broadcast &a¡%player% ha alcanzado el rango Explorador!"
        - "lp user %player% parent set explorador"
      items:
        - "NETHERITE_SWORD:1"
        - "DIAMOND_ARMOR:1"
        - "ENDER_PEARL:16"
      experience: 300
      money: 1500
    cost: 1000

  "3":
    name: "&eMaestro"
    display_name: "&e[Maestro]"
    requirements:
      mob_kills:
        ZOMBIE: 500
        SKELETON: 300
        CREEPER: 100
        ENDERMAN: 25
        BLAZE: 15
        WITHER_SKELETON: 10
      block_breaks:
        STONE: 1000
        IRON_ORE: 200
        GOLD_ORE: 50
        DIAMOND_ORE: 10
        EMERALD_ORE: 5
        ANCIENT_DEBRIS: 2
      playtime_minutes: 720
      fishing:
        COD: 100
        SALMON: 75
        TROPICAL_FISH: 25
        PUFFERFISH: 10
      farming:
        WHEAT: 500
        CARROT: 300
        POTATO: 300
        BEETROOT: 200
        NETHER_WART: 100
    rewards:
      commands:
        - "eco give %player% 10000"
        - "broadcast &a¡%player% ha alcanzado el rango Maestro!"
        - "lp user %player% parent set maestro"
        - "give %player% netherite_ingot 5"
      items:
        - "NETHERITE_SWORD:1:unbreaking:3"
        - "NETHERITE_ARMOR:1"
        - "ELYTRA:1"
      experience: 1000
      money: 5000
    cost: 2500
```

## Ejemplo 3: Sistema de Rangos Temático (Medieval)

```yaml
ranks:
  "1":
    name: "&7Campesino"
    display_name: "&7[Campesino]"
    requirements:
      mob_kills:
        ZOMBIE: 20
        SKELETON: 10
      block_breaks:
        STONE: 30
        DIRT: 100
      farming:
        WHEAT: 25
        CARROT: 15
      playtime_minutes: 30
    rewards:
      commands:
        - "eco give %player% 300"
        - "broadcast &7¡%player% se ha convertido en un humilde Campesino!"
      items:
        - "WOODEN_SWORD:1"
        - "LEATHER_ARMOR:1"
        - "BREAD:32"
      experience: 25
      money: 150
    cost: 0

  "2":
    name: "&aMercenario"
    display_name: "&a[Mercenario]"
    requirements:
      mob_kills:
        ZOMBIE: 80
        SKELETON: 50
        CREEPER: 15
        SPIDER: 30
      block_breaks:
        STONE: 150
        IRON_ORE: 20
      playtime_minutes: 120
    rewards:
      commands:
        - "eco give %player% 800"
        - "broadcast &a¡%player% se ha convertido en un valiente Mercenario!"
      items:
        - "IRON_SWORD:1"
        - "IRON_ARMOR:1"
        - "ARROW:64"
      experience: 100
      money: 400
    cost: 200

  "3":
    name: "&eCaballero"
    display_name: "&e[Caballero]"
    requirements:
      mob_kills:
        ZOMBIE: 200
        SKELETON: 150
        CREEPER: 50
        ENDERMAN: 10
      block_breaks:
        STONE: 400
        IRON_ORE: 80
        GOLD_ORE: 15
      playtime_minutes: 360
    rewards:
      commands:
        - "eco give %player% 2000"
        - "broadcast &e¡%player% se ha convertido en un noble Caballero!"
      items:
        - "DIAMOND_SWORD:1"
        - "DIAMOND_ARMOR:1"
        - "HORSE_SPAWN_EGG:1"
      experience: 300
      money: 1000
    cost: 500

  "4":
    name: "&6Señor Feudal"
    display_name: "&6[Señor Feudal]"
    requirements:
      mob_kills:
        ZOMBIE: 500
        SKELETON: 300
        CREEPER: 150
        ENDERMAN: 30
        BLAZE: 20
      block_breaks:
        STONE: 1000
        IRON_ORE: 200
        GOLD_ORE: 50
        DIAMOND_ORE: 8
      playtime_minutes: 720
    rewards:
      commands:
        - "eco give %player% 5000"
        - "broadcast &6¡%player% se ha convertido en un poderoso Señor Feudal!"
        - "lp user %player% parent set lord"
      items:
        - "NETHERITE_SWORD:1"
        - "NETHERITE_ARMOR:1"
        - "GOLDEN_APPLE:5"
      experience: 800
      money: 2500
    cost: 1000
```

## Ejemplo 4: Sistema de Rangos con Costos

```yaml
ranks:
  "1":
    name: "&7Bronce"
    display_name: "&7[Bronce]"
    requirements:
      mob_kills:
        ZOMBIE: 30
      block_breaks:
        STONE: 50
      playtime_minutes: 45
    rewards:
      commands:
        - "eco give %player% 200"
      items:
        - "IRON_SWORD:1"
      experience: 50
      money: 100
    cost: 0

  "2":
    name: "&aPlata"
    display_name: "&a[Plata]"
    requirements:
      mob_kills:
        ZOMBIE: 100
        SKELETON: 50
      block_breaks:
        STONE: 200
        IRON_ORE: 30
      playtime_minutes: 180
    rewards:
      commands:
        - "eco give %player% 500"
      items:
        - "DIAMOND_SWORD:1"
      experience: 150
      money: 250
    cost: 1000  # Costo en dinero

  "3":
    name: "&eOro"
    display_name: "&e[Oro]"
    requirements:
      mob_kills:
        ZOMBIE: 300
        SKELETON: 200
        CREEPER: 50
      block_breaks:
        STONE: 600
        IRON_ORE: 100
        GOLD_ORE: 20
      playtime_minutes: 480
    rewards:
      commands:
        - "eco give %player% 1200"
      items:
        - "NETHERITE_SWORD:1"
      experience: 400
      money: 600
    cost: 2500

  "4":
    name: "&6Diamante"
    display_name: "&6[Diamante]"
    requirements:
      mob_kills:
        ZOMBIE: 800
        SKELETON: 500
        CREEPER: 200
        ENDERMAN: 30
      block_breaks:
        STONE: 1500
        IRON_ORE: 300
        GOLD_ORE: 80
        DIAMOND_ORE: 15
      playtime_minutes: 1200
    rewards:
      commands:
        - "eco give %player% 3000"
        - "broadcast &6¡%player% ha alcanzado el rango Diamante!"
      items:
        - "NETHERITE_SWORD:1:unbreaking:3"
        - "ELYTRA:1"
      experience: 1000
      money: 1500
    cost: 5000
```

## Ejemplo 5: Configuración de Mensajes Personalizados

```yaml
messages:
  prefix: "&8[&b⚔ VanguardRankUps ⚔&8] &r"
  no_permission: "&c❌ No tienes permisos para usar este comando."
  player_not_found: "&c❌ Jugador no encontrado."
  rankup_success: "&a🎉 ¡Felicidades! Has alcanzado el rango &e%rank%&a!"
  rankup_failed: "&c❌ No cumples los requisitos para el siguiente rango."
  already_max_rank: "&c❌ Ya tienes el rango máximo."
  progress_updated: "&a✅ Progreso actualizado: %progress%"
  config_reloaded: "&a✅ Configuración recargada exitosamente."
  rank_forced: "&a✅ Rango forzado para &e%player%&a."
  rank_reset: "&a✅ Progreso de &e%player%&a reseteado."
  rank_set: "&a✅ Rango de &e%player%&a establecido a &e%rank%&a."
  
  # Mensajes de progreso
  progress_mob_kills: "&c💀 Mobs eliminados: &f%current%&7/&f%required%"
  progress_block_breaks: "&6⛏️ Bloques minados: &f%current%&7/&f%required%"
  progress_playtime: "&e⏰ Tiempo jugado: &f%current%&7/&f%required% minutos"
  progress_fishing: "&b🎣 Pescas: &f%current%&7/&f%required%"
  progress_farming: "&a🌾 Cultivos: &f%current%&7/&f%required%"
```

## Ejemplo 6: Configuración de Sonidos

```yaml
sounds:
  rankup_success: "ENTITY_PLAYER_LEVELUP"
  rankup_failed: "ENTITY_VILLAGER_NO"
  progress_update: "BLOCK_NOTE_BLOCK_PLING"
  rankup_available: "ENTITY_EXPERIENCE_ORB_PICKUP"
  config_reload: "BLOCK_NOTE_BLOCK_CHIME"
```

## Ejemplo 7: Configuración de GUI

```yaml
gui:
  title: "&8⚔ VanguardRankUps - %player% ⚔"
  progress_bar_filled: "&a█"
  progress_bar_empty: "&7█"
  progress_bar_length: 25
  show_percentages: true
  show_progress_bars: true
```

## Notas Importantes

1. **Variables disponibles en comandos:**
   - `%player%` - Nombre del jugador
   - `%rank%` - Nombre del rango alcanzado
   - `%level%` - Nivel del rango

2. **Formato de items:**
   - `MATERIAL:CANTIDAD` - Item básico
   - `MATERIAL:CANTIDAD:ENCHANT:LEVEL` - Item con encantamiento
   - `MATERIAL:CANTIDAD:ENCHANT:LEVEL,ENCHANT2:LEVEL2` - Múltiples encantamientos

3. **Tipos de mobs disponibles:**
   - ZOMBIE, SKELETON, CREEPER, SPIDER, ENDERMAN, BLAZE, etc.

4. **Tipos de bloques disponibles:**
   - STONE, IRON_ORE, GOLD_ORE, DIAMOND_ORE, COAL_ORE, etc.

5. **Tipos de peces disponibles:**
   - COD, SALMON, TROPICAL_FISH, PUFFERFISH

6. **Tipos de cultivos disponibles:**
   - WHEAT, CARROT, POTATO, BEETROOT, NETHER_WART 