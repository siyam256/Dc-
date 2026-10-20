package com.example.security

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.data.DisguiseAlias
import com.example.data.VaultPreferences

object DisguiseManager {

    fun applyDisguise(context: Context, targetAlias: DisguiseAlias): Boolean {
        val pm = context.packageManager
        val pkg = context.packageName

        val aliases = mapOf(
            DisguiseAlias.DEFAULT to ComponentName(pkg, "$pkg.DefaultAlias"),
            DisguiseAlias.CALCULATOR to ComponentName(pkg, "$pkg.CalculatorAlias"),
            DisguiseAlias.NOTES to ComponentName(pkg, "$pkg.NotesAlias"),
            DisguiseAlias.CLOCK to ComponentName(pkg, "$pkg.ClockAlias")
        )

        val targetComponent = aliases[targetAlias] ?: return false

        return try {
            // 1. Enable the target component first so the app is always present in launcher
            pm.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // 2. Disable all other aliases
            aliases.forEach { (alias, component) ->
                if (alias != targetAlias) {
                    pm.setComponentEnabledSetting(
                        component,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }

            VaultPreferences(context).disguiseAlias = targetAlias
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
