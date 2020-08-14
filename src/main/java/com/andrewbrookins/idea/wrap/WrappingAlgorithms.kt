package com.andrewbrookins.idea.wrap

import kotlin.math.pow

/**
 * Helper to wrap a string using a greedy algorithm.
 *
 * Adapted from Apache Commons Lang.
 */
fun wrapGreedy(str: String, wrapLength: Int, lineSeparator: String): String {
    val inputLineLength = str.length
    val space = ' '
    val wrappedLine = StringBuffer(inputLineLength + 32)
    var offset = 0

    while (inputLineLength - offset > wrapLength) {
        if (str[offset] == space) {
            offset += 1
            continue
        }
        if (inputLineLength - offset <= wrapLength) {
            break
        }
        var spaceToWrapAt = str.lastIndexOf(space, wrapLength + offset)

        if (spaceToWrapAt >= offset) {
            wrappedLine.append(str.substring(offset, spaceToWrapAt))
            wrappedLine.append(lineSeparator)
            offset = spaceToWrapAt + 1
        } else {
            spaceToWrapAt = str.indexOf(space, wrapLength + offset)
            if (spaceToWrapAt >= 0) {
                wrappedLine.append(str.substring(offset, spaceToWrapAt))
                wrappedLine.append(lineSeparator)
                offset = spaceToWrapAt + 1
            } else {
                wrappedLine.append(str.substring(offset))
                offset = inputLineLength
            }
        }
    }

    wrappedLine.append(str.substring(offset))
    return wrappedLine.toString()
}

/**
 * Helper to wrap a string using a minimum raggedness algorithm.
 *
 * Based on Aggarwal and Tokuyama's work (1998) via http://xxyxyz.org/line-breaking/
 */
fun wrapMinimumRaggedness(text: String, width: Int): Array<String> {
    val words = text.split(' ')
    val count = words.size
    val offsets = arrayListOf(0.0)
    for (w in words) {
        offsets.add(offsets[offsets.size - 1] + w.length)
    }

    val minima = arrayOf(0.0) + Array(count, { Math.pow(10.0, 20.0) })
    val breaks = Array(count + 1, { 0 })

    fun cost(i: Int, j: Int): Double {
        val w = offsets[j] - offsets[i] + j - i - 1
        if (w > width) {
            return 10.0.pow(10.0) * (w - width)
        }
        return minima[i] + (width - w).pow(2.0)
    }

    fun smawk(rows: MutableList<Int>, columns: Array<Int>) {
        val stack = arrayListOf<Int>()
        var i = 0
        val length = rows.size
        while (i < length) {
            if (stack.size > 0) {
                val c = columns[stack.size - 1]
                if (cost(stack[stack.size - 1], c) < cost(rows[i], c)) {
                    if (stack.size < columns.size) {
                        stack.add(rows[i])
                    }
                    i += 1
                } else {
                    stack.removeAt(stack.size - 1)
                }
            } else {
                stack.add(rows[i])
                i += 1
            }
        }

        if (columns.size > 1) {
            // Equivalent to a Python slice with step: smawk(stack, columns[1::2])
            smawk(stack, columns.copyOfRange(1, columns.size).filter({ columns.indexOf(it) % 2 != 0 }).toTypedArray())
        }

        i = 0
        var j = 0
        var end: Int
        while (j < columns.size) {
            if (j + 1 < columns.size) {
                end = breaks[columns[j + 1]]
            } else {
                end = stack[stack.size - 1]
            }
            val c = cost(stack[i], columns[j])
            if (c < minima[columns[j]]) {
                minima[columns[j]] = c
                breaks[columns[j]] = stack[i]
            }
            if (stack[i] < end) {
                i += 1
            } else {
                j += 2
            }
        }
    }

    var n = count + 1
    var i = 0
    var offset = 0

    while (true) {
        val r = Math.min(n.toDouble(), 2.0.pow((i + 1.0))).toInt()
        val edge = 2.0.pow(i.toDouble()) + offset
        // Python ranges drop the last item, but Kotlin's preserve it -- so we subtract one.
        smawk(((0 + offset) until edge.toInt()).toMutableList(), (edge.toInt() until (r + offset)).toMutableList().toTypedArray())
        val x = minima[(r - 1 + offset)]
        var costGreaterThanOrEqualToMinima = false

        for (j in 2.0.pow(i.toDouble()).toInt() until r - 1) {
            val y = cost(j + offset, r - 1 + offset)
            if (y <= x) {
                n -= j
                i = 0
                offset += j
                costGreaterThanOrEqualToMinima = true
                break
            }
        }
        if (!costGreaterThanOrEqualToMinima) {
            if (r == n) {
                break
            }
            i += 1
        }
    }

    val lines: MutableList<String> = arrayListOf()
    var j = count
    while (j > 0) {
        i = breaks[j]
        // Python ranges drop the last item, but Kotlin's preserve it -- so we subtract one.
        lines.add(words.slice(i until j).joinToString(" "))
        j = i
    }
    return lines.reversed().toTypedArray()
}

