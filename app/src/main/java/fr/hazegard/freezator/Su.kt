package fr.hazegard.freezator

import android.util.Log
import fr.hazegard.freezator.exception.NotRootException
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * Created by Hazegard on 01/03/18.
 */

/**
 * The class manage a root process
 * This class is a singleton in order to prevent the app from requesting several root processes
 */
class Su private constructor() {
    private val su: Process = getSuProcess()
    private val os = DataOutputStream(su.outputStream)
    private val osRes = DataInputStream(su.inputStream)
    private val osErr = DataInputStream(su.errorStream)

    /**
     * Execute the command given in a root process
     * @param command String: The command to execute
     * @return Output of the command
     */
    fun exec(command: String): String {
        writeInput(command)
        return readOutput()
    }

    /**
     * Get a root process
     * This method can throw NotRootedException if it cannot get a su shell
     * @return A su process
     * @throws NotRootException
     */
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

    /**
     * Get the dataStream from a process
     * @param process the process
     * @return (output, input, error)
     */
    private fun getDataStream(process: Process): Triple<DataOutputStream, DataInputStream, DataInputStream> {
        val dos = DataOutputStream(process.outputStream)
        val dis = DataInputStream(process.inputStream)
        val err = DataInputStream(process.errorStream)
        return Triple(dos, dis, err)
    }

    /**
     * Read the output of the shell process
     * @return Output of the process
     */
    private fun readOutput(): String {
        val buffer = BufferedReader(osRes.bufferedReader())
        val response = StringBuffer()
        var line = buffer.readLine()
        while (EOF != line) {
            response.append(line)
            response.append("\n")
            line = buffer.readLine()
        }
        return response.toString().trim()
    }

    /**
     * Write the input to the shell process
     * @param input Input to write in the shell
     */
    private fun writeInput(input: String) {
        os.writeBytes("$input && echo \"$EOF\"\n")
        os.flush()
    }

    companion object {
        val instance by lazy { Su() }
        private const val EOF = "EOF"
    }
}