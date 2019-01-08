package fr.hazegard.freezator

import android.util.Log
import fr.hazegard.freezator.exception.NotRootException
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import kotlin.collections.ArrayList

/**
 * Created by maxime on 01/03/18.
 */
class Su private constructor() {
    private val su: Process = getSuProcess()
    private val os = DataOutputStream(su.outputStream)
    private val osRes = DataInputStream(su.inputStream)

    fun exec(command: String): String {
        writeInput(command)
        return readOutput()
    }

    @Throws
    private fun getSuProcess(): Process {
        try {
            val su: Process = Runtime.getRuntime().exec("su")
            val (os, osRes, osErr) = getDataStream(su)
            os.writeBytes("id -u\n")
            os.flush()
            val currUid: String? = BufferedReader(osRes.bufferedReader()).readLine()
            if (currUid?.trim()?.toInt() != 0) {
                throw NotRootException("No Su process")
            } else {
                Log.d("Process", "su process granted")
                return su
            }
        } catch (e: IOException) {
            throw NotRootException("No Su process")
        }
    }

    private fun getDataStream(process: Process): Triple<DataOutputStream, DataInputStream, DataInputStream> {
        val dos = DataOutputStream(process.outputStream)
        val dis = DataInputStream(process.inputStream)
        val err = DataInputStream(process.errorStream)
        return Triple(dos, dis, err)
    }

    private fun readOutput(): String {
        val buffer = BufferedReader(osRes.bufferedReader())
        val response = StringBuffer()
        var line = buffer.readLine()
        while ("EOF" != line) {
            response.append(line)
            response.append("\n")
            line = buffer.readLine()
        }
        return response.toString().trim()
    }

    private fun writeInput(input: String) {
        os.writeBytes("$input && echo \"EOF\"\n")
        os.flush()
    }

    companion object {
        val instance by lazy { Su() }
    }
}