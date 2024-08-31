package org.oar.bytes.features.time

interface TimeControlled {
    /**
     *
     * @param newStartTime time when TimeController.startTimeData was called
     * @param relShutdownTime time since TimeController.startTimeData was called
     * @param timePassed offline time passed (only if 'countOfflineTime' flag is true)
     * @return false to unregister from TimeController
     */
    fun notifyOfflineTime(newStartTime: Long, relShutdownTime: Long, timePassed: Long): Boolean
}