package com.mobileagent.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class DatabaseInfo(
    val path: String,
    val name: String,
    val size: Long,
    val tables: List<String>
)

data class TableInfo(
    val name: String,
    val columns: List<ColumnInfo>,
    val rowCount: Int
)

data class ColumnInfo(
    val name: String,
    val type: String,
    val notNull: Boolean,
    val defaultValue: String?,
    val primaryKey: Boolean
)

data class QueryResult(
    val columns: List<String>,
    val rows: List<Map<String, Any?>>,
    val rowCount: Int,
    val executionTime: Long
)

class DatabaseManager {
    private var currentDatabase: SQLiteDatabase? = null
    private var currentDatabasePath: String? = null

    suspend fun openDatabase(path: String): Result<DatabaseInfo> = withContext(Dispatchers.IO) {
        try {
            closeDatabase()

            val file = File(path)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }

            currentDatabase = SQLiteDatabase.openDatabase(
                path,
                null,
                SQLiteDatabase.OPEN_READWRITE
            )
            currentDatabasePath = path

            val tables = getTables()
            val info = DatabaseInfo(
                path = path,
                name = file.name,
                size = file.length(),
                tables = tables
            )

            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun closeDatabase() {
        currentDatabase?.close()
        currentDatabase = null
        currentDatabasePath = null
    }

    suspend fun executeQuery(sql: String): Result<QueryResult> = withContext(Dispatchers.IO) {
        try {
            val db = currentDatabase ?: return@withContext Result.failure(
                Exception("No database open")
            )

            val startTime = System.currentTimeMillis()
            val cursor: Cursor = db.rawQuery(sql, null)
            val executionTime = System.currentTimeMillis() - startTime

            val columns = cursor.columnNames.toList()
            val rows = mutableListOf<Map<String, Any?>>()

            while (cursor.moveToNext()) {
                val row = mutableMapOf<String, Any?>()
                columns.forEachIndexed { index, columnName ->
                    row[columnName] = when (cursor.getType(index)) {
                        Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(index)
                        Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(index)
                        Cursor.FIELD_TYPE_STRING -> cursor.getString(index)
                        Cursor.FIELD_TYPE_BLOB -> cursor.getBlob(index)
                        Cursor.FIELD_TYPE_NULL -> null
                        else -> cursor.getString(index)
                    }
                }
                rows.add(row)
            }

            cursor.close()

            Result.success(
                QueryResult(
                    columns = columns,
                    rows = rows,
                    rowCount = rows.size,
                    executionTime = executionTime
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun executeUpdate(sql: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = currentDatabase ?: return@withContext Result.failure(
                Exception("No database open")
            )

            db.execSQL(sql)
            Result.success("Query executed successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getTables(): List<String> {
        val db = currentDatabase ?: return emptyList()
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
            null
        )

        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()

        return tables
    }

    suspend fun getTableInfo(tableName: String): Result<TableInfo> = withContext(Dispatchers.IO) {
        try {
            val db = currentDatabase ?: return@withContext Result.failure(
                Exception("No database open")
            )

            // Get column info
            val cursor = db.rawQuery("PRAGMA table_info($tableName)", null)
            val columns = mutableListOf<ColumnInfo>()

            while (cursor.moveToNext()) {
                columns.add(
                    ColumnInfo(
                        name = cursor.getString(1),
                        type = cursor.getString(2),
                        notNull = cursor.getInt(3) == 1,
                        defaultValue = cursor.getString(4),
                        primaryKey = cursor.getInt(5) == 1
                    )
                )
            }
            cursor.close()

            // Get row count
            val countCursor = db.rawQuery("SELECT COUNT(*) FROM $tableName", null)
            var rowCount = 0
            if (countCursor.moveToFirst()) {
                rowCount = countCursor.getInt(0)
            }
            countCursor.close()

            Result.success(
                TableInfo(
                    name = tableName,
                    columns = columns,
                    rowCount = rowCount
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTable(
        tableName: String,
        columns: List<ColumnInfo>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = currentDatabase ?: return@withContext Result.failure(
                Exception("No database open")
            )

            val columnDefs = columns.joinToString(", ") { col ->
                buildString {
                    append(col.name)
                    append(" ")
                    append(col.type)
                    if (col.primaryKey) append(" PRIMARY KEY")
                    if (col.notNull) append(" NOT NULL")
                    col.defaultValue?.let { append(" DEFAULT $it") }
                }
            }

            val sql = "CREATE TABLE $tableName ($columnDefs)"
            db.execSQL(sql)

            Result.success("Table created: $tableName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun dropTable(tableName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = currentDatabase ?: return@withContext Result.failure(
                Exception("No database open")
            )

            db.execSQL("DROP TABLE IF EXISTS $tableName")
            Result.success("Table dropped: $tableName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertRow(
        tableName: String,
        values: Map<String, Any?>
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val db = currentDatabase ?: return@withContext Result.failure(
                Exception("No database open")
            )

            val columns = values.keys.joinToString(", ")
            val placeholders = values.keys.joinToString(", ") { "?" }
            val sql = "INSERT INTO $tableName ($columns) VALUES ($placeholders)"

            val statement = db.compileStatement(sql)
            values.values.forEachIndexed { index, value ->
                when (value) {
                    is String -> statement.bindString(index + 1, value)
                    is Long -> statement.bindLong(index + 1, value)
                    is Double -> statement.bindDouble(index + 1, value)
                    is ByteArray -> statement.bindBlob(index + 1, value)
                    null -> statement.bindNull(index + 1)
                    else -> statement.bindString(index + 1, value.toString())
                }
            }

            val rowId = statement.executeInsert()
            Result.success(rowId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRow(
        tableName: String,
        values: Map<String, Any?>,
        whereClause: String,
        whereArgs: Array<String>
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val db = currentDatabase ?: return@withContext Result.failure(
                Exception("No database open")
            )

            val contentValues = android.content.ContentValues()
            values.forEach { (key, value) ->
                when (value) {
                    is String -> contentValues.put(key, value)
                    is Long -> contentValues.put(key, value)
                    is Int -> contentValues.put(key, value)
                    is Double -> contentValues.put(key, value)
                    is ByteArray -> contentValues.put(key, value)
                    null -> contentValues.putNull(key)
                    else -> contentValues.put(key, value.toString())
                }
            }

            val rowsAffected = db.update(tableName, contentValues, whereClause, whereArgs)
            Result.success(rowsAffected)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRow(
        tableName: String,
        whereClause: String,
        whereArgs: Array<String>
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val db = currentDatabase ?: return@withContext Result.failure(
                Exception("No database open")
            )

            val rowsDeleted = db.delete(tableName, whereClause, whereArgs)
            Result.success(rowsDeleted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportToSQL(outputPath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = currentDatabase ?: return@withContext Result.failure(
                Exception("No database open")
            )

            val output = StringBuilder()
            val tables = getTables()

            tables.forEach { tableName ->
                // Get create statement
                val cursor = db.rawQuery(
                    "SELECT sql FROM sqlite_master WHERE type='table' AND name=?",
                    arrayOf(tableName)
                )

                if (cursor.moveToFirst()) {
                    output.appendLine(cursor.getString(0) + ";")
                    output.appendLine()
                }
                cursor.close()

                // Get data
                val dataCursor = db.rawQuery("SELECT * FROM $tableName", null)
                val columns = dataCursor.columnNames

                while (dataCursor.moveToNext()) {
                    val values = columns.map { col ->
                        val index = dataCursor.getColumnIndex(col)
                        when (dataCursor.getType(index)) {
                            Cursor.FIELD_TYPE_STRING -> "'${dataCursor.getString(index)}'"
                            Cursor.FIELD_TYPE_INTEGER -> dataCursor.getLong(index).toString()
                            Cursor.FIELD_TYPE_FLOAT -> dataCursor.getDouble(index).toString()
                            Cursor.FIELD_TYPE_NULL -> "NULL"
                            else -> "'${dataCursor.getString(index)}'"
                        }
                    }

                    output.appendLine(
                        "INSERT INTO $tableName (${columns.joinToString(", ")}) VALUES (${values.joinToString(", ")});"
                    )
                }
                dataCursor.close()
                output.appendLine()
            }

            File(outputPath).writeText(output.toString())
            Result.success("Database exported to $outputPath")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isOpen(): Boolean = currentDatabase?.isOpen == true

    fun getCurrentDatabasePath(): String? = currentDatabasePath
}
