package com.thelightphone.games

import com.thelightphone.sdk.EntryPoint
import com.thelightphone.sdk.LightEntryPoint
import com.thelightphone.sdk.shared.LightServerData
import kotlinx.coroutines.flow.StateFlow

/**
 * Games is fully on-device: no push notifications, no server round-trips.
 * We still need to satisfy the SDK's required entry point contract.
 */
@EntryPoint
object ToolEntryPoint : LightEntryPoint {
    override suspend fun onToolCreate(serverData: StateFlow<LightServerData?>) {
        // Nothing to initialize - no backend for this tool.
    }
}
