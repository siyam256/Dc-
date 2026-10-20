package com.example.security

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.data.DisguiseAlias
import com.example.data.VaultPreferences

object DisguiseManager {

    fun applyDisguise(context: Context, targetAlias: DisguiseAlias) {
        val pm = context.packageManager
        val pkg = context.packageName

        val aliases = mapOf(
            DisguiseAlias.DEFAULT to ComponentName(pkg, "$pkg.MainActivity"),
            DisguiseAlias.CALCULATOR to ComponentName(pkg, "$pkg.CalculatorAlias"),
            DisguiseAlias.NOTES to ComponentName(pkg, "$pkg.NotesAlias"),
            DisguiseAlias.CLOCK to ComponentName(pkg, "$pkg.ClockAlias")
        )

        aliases.forEach { (alias, component) ->
            val newState = if (alias == targetAlias) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            try {
                pm.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        VaultPreferences(context).disguiseAlias = targetAlias
    }
}
