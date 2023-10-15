package org.oar.bytes.utils

import java.util.*

class ConcurrentMutableList<T> : MutableList<T> by Collections.synchronizedList(mutableListOf())