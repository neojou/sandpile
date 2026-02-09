package com.neojou

import kotlin.time.TimeSource

/**
 * Logging severity level used by [MyLog].
 *
 * A larger [priority] means a more severe event.
 *
 * @property priority Numeric severity used for filtering (greater-or-equal passes).
 */
enum class LogLevel(val priority: Int) {
    /** Verbose information for development and troubleshooting. */
    DEBUG(0),
    /** General informational messages about normal operation. */
    INFO(1),
    /** Potential problems or notable situations that aren't errors yet. */
    WARN(2),
    /** Error conditions that indicate a failure or loss of functionality. */
    ERROR(3)
}

/**
 * A single log record captured by [MyLog].
 *
 * @property elapsed Elapsed time since [MyLog] start mark, formatted as a string.
 * @property level Severity of this entry.
 * @property module Logical module/tag name used for filtering and module overrides.
 * @property message Human-readable message content.
 */
data class LogEntry(
    val elapsed: String,
    val level: LogLevel,
    val module: String,
    val message: String
)

/**
 * Simple in-memory logger with optional console output.
 *
 * Features:
 * - Stores log entries in memory for later inspection via [getAll] / [getFiltered].
 * - Filters console printing using a global minimum level and per-module overrides.
 * - Uses a monotonic clock ([TimeSource.Monotonic]) to compute elapsed times.
 *
 * Notes:
 * - This implementation is not synchronized; if used from multiple threads, consider adding
 *   synchronization around [entries] and configuration mutations.
 */
object MyLog {
    /**
     * Whether console printing is enabled.
     *
     * Even when disabled, entries are still collected in [entries].
     */
    private var consoleOn: Boolean = true

    /**
     * In-memory storage of all collected log entries.
     */
    private val entries: MutableList<LogEntry> = mutableListOf()

    /**
     * Global minimum level for console output (default: [LogLevel.INFO]).
     *
     * Per-module overrides in [moduleMinLevels] take precedence.
     */
    private var globalMinLevel: LogLevel = LogLevel.INFO

    /**
     * Per-module minimum levels for console output.
     *
     * Example: `"AI" -> LogLevel.DEBUG`.
     */
    private val moduleMinLevels: MutableMap<String, LogLevel> = mutableMapOf()

    /**
     * Baseline mark captured at logger initialization, using monotonic time.
     */
    // 程式啟動基準點（單調時間）
    private val startMark = TimeSource.Monotonic.markNow()

    /**
     * Enables console output for entries that pass filtering rules.
     */
    fun turnOnConsole() { consoleOn = true }

    /**
     * Disables console output.
     *
     * Entries are still stored and can be retrieved via [getAll] / [getFiltered].
     */
    fun turnOffConsole() { consoleOn = false }

    /**
     * Sets the global minimum severity level for console printing.
     *
     * @param level Minimum level required to print, unless overridden per-module.
     * @see setModuleMinLevel
     */
    fun setGlobalMinLevel(level: LogLevel) { globalMinLevel = level }

    /**
     * Sets/overrides the minimum severity level for a given [module].
     *
     * @param module Module/tag name used when calling [add].
     * @param level Minimum level required to print for this module.
     */
    fun setModuleMinLevel(module: String, level: LogLevel) {
        moduleMinLevels[module] = level
    }

    /**
     * Clears the minimum-level override for [module], reverting it to [globalMinLevel].
     *
     * @param module Module/tag name previously configured by [setModuleMinLevel].
     */
    fun clearModuleMinLevel(module: String) {
        moduleMinLevels.remove(module)
    }

    /**
     * Returns whether an entry of [level] from [module] should be printed to console.
     */
    private fun shouldPrint(level: LogLevel, module: String): Boolean {
        if (!consoleOn) return false
        val minLevel = moduleMinLevels[module] ?: globalMinLevel
        return level.priority >= minLevel.priority
    }

    /**
     * Adds a log entry.
     *
     * The entry is always appended to the in-memory list; console printing depends on
     * the current filtering rules (see [setGlobalMinLevel] and [setModuleMinLevel]).
     *
     * @param module Module/tag name for filtering (for example: `"APP"`, `"AI"`).
     * @param message Message text to record.
     * @param level Severity level (default: [LogLevel.INFO]).
     */
    fun add(module: String, message: String, level: LogLevel = LogLevel.INFO) {
        val entry = LogEntry(
            elapsed = startMark.elapsedNow().toString(),
            level = level,
            module = module,
            message = message
        )
        entries.add(entry)

        if (shouldPrint(level, module)) {
            println("[${entry.elapsed}] [${entry.level}] [${entry.module}] ${entry.message}")
        }
    }

    /**
     * Returns an immutable snapshot of all collected entries.
     */
    fun getAll(): List<LogEntry> = entries.toList()

    /**
     * Returns entries filtered by optional [module] and minimum severity [minLevel].
     *
     * @param module If non-null, only entries whose [LogEntry.module] equals this value are returned.
     * @param minLevel Minimum severity to include (inclusive).
     * @return A list of entries matching the filter.
     */
    fun getFiltered(module: String? = null, minLevel: LogLevel = LogLevel.DEBUG): List<LogEntry> {
        return entries.filter {
            (module == null || it.module == module) && it.level.priority >= minLevel.priority
        }
    }

    /**
     * Clears all collected entries from memory.
     */
    fun clear() { entries.clear() }
}
