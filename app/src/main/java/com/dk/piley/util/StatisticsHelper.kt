package com.dk.piley.util

import android.content.Context
import com.dk.piley.R
import com.dk.piley.model.pile.PileWithTasks
import com.dk.piley.model.task.Task
import com.dk.piley.model.task.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToLong

/**
 * Get frequencies of completed tasks for dates
 *
 * @param pileWithTasks pile with tasks to get the frequencies for
 * @return map of date keys and completed task frequency values
 */
fun pileFrequenciesForDates(pileWithTasks: PileWithTasks): Map<LocalDate, Int> =
    pileWithTasks.tasks
        .filter {
            it.status == TaskStatus.DONE && LocalDateTime.now().minusWeeks(1)
                .isBefore(it.modifiedAt.toLocalDateTime())
        } // only include completed tasks from the past week
        .map { it.completionTimes } // mop to lists of completion times
        .flatten() // flatten lists into a single list of completion times
        .groupingBy { it.toLocalDateTime().toLocalDate() } // group by completion date
        .eachCount()

/**
 * Get completed task frequencies for the last 7 days
 *
 * @param frequencyMap calculated frequency map of completed tasks using [pileFrequenciesForDates]
 * @return map of date keys within the last 7 days and completed task counts as values
 */
fun pileFrequenciesForDatesWithZerosForLast7Days(frequencyMap: Map<LocalDate, Int>): Map<LocalDate, Int> {
    val finalMap = mutableMapOf<LocalDate, Int>()
    for (i in 0..6) {
        val date = LocalDateTime.now().minusDays(i.toLong()).toLocalDate()
        val existingValue = frequencyMap[date]
        val mapValue = existingValue ?: 0
        finalMap[date] = mapValue
    }
    return finalMap
}

/**
 * Get list of completed task counts for the past 7 days
 *
 * @param pileWithTasks pile with tasks to calculate the completion counts for
 * @return a list of completion frequencies for each of the past 7 days in reverse (starting with today)
 */
fun getCompletedTasksForWeekValues(pileWithTasks: PileWithTasks): List<Int> =
    pileFrequenciesForDatesWithZerosForLast7Days(
        pileFrequenciesForDates(pileWithTasks)
    ).values.toList().reversed()


/**
 * Get list of 3 most upcoming/urgent tasks (nearest reminder times)
 *
 * @param pilesWithTasks list of piles with tasks to get the tasks from
 * @return list of pairs containing the pile name and the task
 */
fun getUpcomingTasks(pilesWithTasks: List<PileWithTasks>): List<Pair<String, Task>> =
    pilesWithTasks.flatMap { pileWithTasks ->
        pileWithTasks.tasks
            .filter {
                (it.reminder != null && it.status == TaskStatus.DEFAULT)
                        || (it.reminder != null && it.isRecurring && it.status != TaskStatus.DELETED)
            }
            .map { Pair(pileWithTasks.pile.name, it) }
    }.sortedBy { it.second.reminder }.take(3)

/**
 * Get the name of the pile with the most uncompleted tasks
 *
 * @param pilesWithTasks list of piles with tasks to find the biggest pile in
 * @param context generic context to get string resources
 * @return name of the biggest pile or "None" if no pile found or list is empty
 */
fun getBiggestPileName(pilesWithTasks: List<PileWithTasks>, context: Context): String =
    pilesWithTasks.maxByOrNull { pileWithTasks ->
        pileWithTasks.tasks.count { it.status == TaskStatus.DEFAULT }
    }?.pile?.name ?: context.getString(R.string.no_pile)

/**
 * Get average task completion time in hours
 *
 * @param pilesWithTasks list of piles with tasks to calculate the average task completion time for
 * @return average completion time in hours or 0 if no completed tasks found
 */
fun getAverageTaskCompletionInHours(pilesWithTasks: List<PileWithTasks>): Long {
    val avg = pilesWithTasks // TODO add filter for done
        .map { pile -> if (pile.tasks.isEmpty()) 0 else pile.tasks.sumOf { it.averageCompletionTimeInHours } / pile.tasks.size }
        .average()
    return if (avg.isNaN()) 0 else avg.roundToLong()
}