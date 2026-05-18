# PinkyRankUps

Un plugin completo y configurable de rankups para servidores de Minecraft, desarrollado por PinkyCore.

## 🚀 Características

### ✅ Sistema de Tareas Configurables
- **Mob Kills**: Matar zombies, esqueletos, creepers, etc.
- **Block Mining**: Picar piedra, minerales, bloques específicos
- **Playtime**: Tiempo de juego requerido
- **Fishing**: Pescas específicas
- **Farming**: Cultivos y cosechas

### 🎮 Comandos Principales
- `/rankup` - Comando principal para rankups
- `/rankup info` - Ver información del rango actual
- `/rankup progress` - Ver progreso detallado
- `/rankup help` - Mostrar ayuda

### 🔧 Comandos de Administración
- `/rankupadmin reload` - Recargar configuración
- `/rankupadmin force <jugador>` - Forzar rankup
- `/rankupadmin reset <jugador>` - Resetear progreso
- `/rankupadmin setrank <jugador> <rango>` - Establecer rango
- `/rankupadmin info <jugador>` - Ver información de jugador

### 💎 Sistema de Recompensas
- **Comandos**: Ejecutar comandos al rankup
- **Items**: Dar items específicos
- **Experiencia**: Otorgar XP
- **Dinero**: Recompensas monetarias (requiere Vault)

### 🗄️ Base de Datos
- **SQLite**: Base de datos local por defecto
- **MySQL**: Soporte para base de datos remota
- **Persistencia**: Datos guardados automáticamente

### 🎨 Interfaz Moderna
- Mensajes coloridos y formateados
- Barras de progreso visuales
- Sonidos personalizables
- Sistema de permisos completo

### 🔗 Integración con LuckPerms
- **Gestión automática de grupos**: Añade/elimina grupos automáticamente
- **Respeto de prefixes**: Respeta prefixes configurados en LuckPerms
- **Configuración flexible**: Opción para usar prefixes del plugin o de LP
- **Fallback a comandos**: Si la API no está disponible, usa comandos de consola

## 📦 Instalación

### Requisitos
- **Minecraft**: 1.20.4+
- **Java**: 17+
- **Servidor**: Spigot/Paper

### Pasos de Instalación

1. **Descargar el plugin**
   ```bash
   # Clonar el repositorio
   git clone https://github.com/Pinky-Core/PinkyRankUps.git
   cd VanguardRankUps
   
   # Compilar con Maven
   mvn clean package
   ```

2. **Instalar en el servidor**
   - Copiar `target/PinkyRankUps` a la carpeta `plugins/`
   - Reiniciar el servidor

3. **Configurar el plugin**
   - Editar `plugins/PinkyRankUps/config.yml`
   - Personalizar rangos, requisitos y recompensas

## ⚙️ Configuración

### Ejemplo de Configuración Básica

```yaml
# Configuración de rangos
ranks:
  "1":
    name: "&7Novato"
    display_name: "&7[Novato]"
    requirements:
      mob_kills:
        ZOMBIE: 50
        SKELETON: 30
      block_breaks:
        STONE: 100
        COAL_ORE: 20
      playtime_minutes: 60
    rewards:
      commands:
        - "eco give %player% 1000"
        - "broadcast &a¡%player% ha alcanzado el rango Novato!"
      items:
        - "DIAMOND_SWORD:1"
      experience: 100
      money: 500
    cost: 0
```

### Configuración de LuckPerms

#### Configuración Básica
```yaml
settings:
  luckperms:
    enabled: true
    remove_previous_group: true
    respect_luckperms_prefix: true
```

#### Configuración de Rangos con Grupos
```yaml
ranks:
  "1":
    name: "&7Novato"
    display_name: "&7[Novato]"
    luckperms_group: "novato"
    # ... resto de configuración
  "2":
    name: "&aAprendiz"
    display_name: "&a[Aprendiz]"
    luckperms_group: "aprendiz"
    # ... resto de configuración
```

#### Opciones de Configuración
- **`enabled`**: Habilita/deshabilita la integración con LuckPerms
- **`remove_previous_group`**: Si es `true`, elimina el grupo del rango anterior al hacer rankup
- **`respect_luckperms_prefix`**: Si es `true`, respeta los prefixes configurados en LuckPerms

### Tipos de Tareas Disponibles

#### Mob Kills
```yaml
mob_kills:
  ZOMBIE: 100
  SKELETON: 50
  CREEPER: 25
  ENDERMAN: 10
  BLAZE: 5
```

#### Block Mining
```yaml
block_breaks:
  STONE: 500
  IRON_ORE: 100
  GOLD_ORE: 25
  DIAMOND_ORE: 5
  EMERALD_ORE: 3
```

#### Playtime
```yaml
playtime_minutes: 360  # 6 horas
```

#### Fishing
```yaml
fishing:
  COD: 50
  SALMON: 25
  TROPICAL_FISH: 10
```

#### Farming
```yaml
farming:
  WHEAT: 100
  CARROT: 50
  POTATO: 50
  BEETROOT: 25
```

## 🔐 Permisos

### Permisos de Jugador
- `vanguardrankups.rankup` - Usar comando /rankup (por defecto: true)

### Permisos de Administrador
- `vanguardrankups.admin` - Acceso completo a comandos admin (por defecto: op)
- `vanguardrankups.force` - Forzar rankups
- `vanguardrankups.reload` - Recargar configuración
- `vanguardrankups.reset` - Resetear progreso
- `vanguardrankups.setrank` - Establecer rangos

## 🎯 Uso

### Para Jugadores

1. **Ver información del rango**
   ```
   /rankup
   ```

2. **Ver progreso detallado**
   ```
   /rankup progress
   ```

3. **Hacer rankup**
   ```
   /rankup
   ```

### Para Administradores

1. **Recargar configuración**
   ```
   /rankupadmin reload
   ```

2. **Forzar rankup de un jugador**
   ```
   /rankupadmin force <jugador>
   ```

3. **Resetear progreso**
   ```
   /rankupadmin reset <jugador>
   ```

4. **Establecer rango específico**
   ```
   /rankupadmin setrank <jugador> <rango>
   ```

## 🗄️ Base de Datos

### SQLite (Por defecto)
```yaml
database:
  type: "sqlite"
  sqlite:
    file: "rankups.db"
```

### MySQL
```yaml
database:
  type: "mysql"
  mysql:
    host: "localhost"
    port: 3306
    database: "rankups"
    username: "root"
    password: "password"
```

## 🎨 Personalización

### Mensajes
```yaml
messages:
  prefix: "&8[&bVanguardRankUps&8] &r"
  rankup_success: "&a¡Felicidades! Has alcanzado el rango %rank%!"
  rankup_failed: "&cNo cumples los requisitos para el siguiente rango."
```

### Sonidos
```yaml
sounds:
  rankup_success: "ENTITY_PLAYER_LEVELUP"
  rankup_failed: "ENTITY_VILLAGER_NO"
  progress_update: "BLOCK_NOTE_BLOCK_PLING"
```

## 🔧 Desarrollo

### Compilar desde el código fuente

```bash
# Clonar repositorio
git clone https://github.com/lewisainsworth/VanguardRankUps.git
cd VanguardRankUps

# Compilar
mvn clean package

# El JAR se generará en target/VanguardRankUps-1.0.0.jar
```

### Estructura del Proyecto

```
VanguardRankUps/
├── src/main/java/com/lewisainsworth/vanguardrankups/
│   ├── VanguardRankUps.java              # Clase principal
│   ├── commands/                         # Comandos
│   │   ├── RankupCommand.java
│   │   └── RankupAdminCommand.java
│   ├── listeners/                        # Event listeners
│   │   └── PlayerListener.java
│   ├── managers/                         # Managers del sistema
│   │   ├── ConfigManager.java
│   │   ├── DatabaseManager.java
│   │   ├── RankupManager.java
│   │   └── TaskManager.java
│   ├── models/                           # Modelos de datos
│   │   ├── PlayerData.java
│   │   └── Rank.java
│   └── utils/                            # Utilidades
│       └── MessageUtils.java
├── src/main/resources/
│   ├── plugin.yml                        # Metadata del plugin
│   └── config.yml                        # Configuración por defecto
├── pom.xml                               # Configuración Maven
└── README.md                             # Este archivo
```

## 🐛 Solución de Problemas

### Problemas Comunes

1. **Plugin no se carga**
   - Verificar que Java 17+ esté instalado
   - Revisar logs del servidor para errores

2. **Base de datos no funciona**
   - Verificar permisos de escritura en la carpeta plugins
   - Para MySQL, verificar conexión y credenciales

3. **Comandos no funcionan**
   - Verificar permisos del jugador
   - Revisar configuración de comandos en plugin.yml

### Logs de Debug

Para habilitar logs de debug:
```yaml
settings:
  debug: true
```

## 📝 Changelog

### v1.0.0
- ✅ Sistema de rankups completo
- ✅ Tareas configurables (mobs, bloques, tiempo, pesca, cultivos)
- ✅ Comandos de administración
- ✅ Base de datos SQLite/MySQL
- ✅ Sistema de recompensas
- ✅ Interfaz moderna con barras de progreso
- ✅ Sonidos personalizables
- ✅ Sistema de permisos completo

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 👨‍💻 Autor

**Lewis Ainsworth**
- GitHub: [@Pinky-Core](https://github.com/Pinky-Core)

## 🙏 Agradecimientos

- Comunidad de Spigot/Paper
- Contribuidores y testers
- Inspiración de otros plugins de rankups

---

**¡Disfruta usando VanguardRankUps en tu servidor!** 🎮 
