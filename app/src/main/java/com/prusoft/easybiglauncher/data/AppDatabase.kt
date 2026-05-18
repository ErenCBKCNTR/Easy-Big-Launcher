package com.prusoft.easybiglauncher.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [LauncherPage::class, LauncherItem::class, Reminder::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun launcherDao(): LauncherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "launcher_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Using manual SQL since getting DAO inside callback can be tricky without the instance
                        db.execSQL("INSERT INTO launcher_pages (pageOrder, rowCount, columnCount) VALUES (0, 3, 2)")
                        // We assume pageId will be 1 for the first page
                        db.execSQL("INSERT INTO launcher_items (pageId, slotIndex, itemType, packageName, label) VALUES (1, 0, 'APP', 'com.android.dialer', 'Telefon')")
                        db.execSQL("INSERT INTO launcher_items (pageId, slotIndex, itemType, packageName, label) VALUES (1, 1, 'APP', 'com.android.messaging', 'Mesajlar')")
                        // Fill remaining slots as EMPTY
                        for (i in 2 until 6) {
                            db.execSQL("INSERT INTO launcher_items (pageId, slotIndex, itemType) VALUES (1, $i, 'EMPTY')")
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
