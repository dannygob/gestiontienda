@androidx.room.Database(
    entities = [], // Empty for now
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    // Comment out DAOs for now
}