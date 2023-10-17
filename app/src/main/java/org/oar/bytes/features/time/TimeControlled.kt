package org.oar.bytes.features.time

interface TimeControlled {
    /**
     *
     * @param newStartTime
     * @param relShutdownTime
     * @param timePassed
     * @return false to unregister from TimeController
     */
    fun notifyOfflineTime(newStartTime: Long, relShutdownTime: Long, timePassed: Long): Boolean
}