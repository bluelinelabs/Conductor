package com.bluelinelabs.conductor.internal

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.bluelinelabs.conductor.ActivityHostedRouter
import com.bluelinelabs.conductor.Router
import kotlinx.parcelize.Parcelize

internal interface LifecycleHandler {
  val routers: List<Router>
  val lifecycleActivity: Activity?
  fun getRouter(container: ViewGroup, savedInstanceState: Bundle?): Router
  fun registerActivityListener(activity: Activity)
  fun registerForActivityResult(instanceId: String, requestCode: Int)
  fun unregisterForActivityResults(instanceId: String)

  fun startActivity(intent: Intent?)
  fun startActivityForResult(instanceId: String, intent: Intent, requestCode: Int, options: Bundle? = null)

  @Throws(IntentSender.SendIntentException::class)
  fun startIntentSenderForResult(
    instanceId: String,
    intent: IntentSender,
    requestCode: Int,
    fillInIntent: Intent?,
    flagsMask: Int,
    flagsValues: Int,
    extraFlags: Int,
    options: Bundle?,
  )

  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
  fun requestPermissions(instanceId: String, permissions: Array<String>, requestCode: Int)

  companion object {
    fun install(activity: Activity, allowAndroidXBacking: Boolean = true): LifecycleHandler {
      var lifecycleHandler = findInActivity(activity, allowAndroidXBacking)
      if (lifecycleHandler == null) {
        if (allowAndroidXBacking && activity is FragmentActivity) {
          lifecycleHandler = AndroidXLifecycleHandlerImpl()
          activity.supportFragmentManager.beginTransaction().add(lifecycleHandler, FRAGMENT_TAG).commit()
        } else {
          lifecycleHandler = PlatformLifecycleHandlerImpl()
          @Suppress("DEPRECATION")
          activity.fragmentManager.beginTransaction().add(lifecycleHandler, FRAGMENT_TAG).commit()
        }
      }
      lifecycleHandler.registerActivityListener(activity)
      return lifecycleHandler
    }
  }
}

internal class AndroidXLifecycleHandlerImpl : androidx.fragment.app.Fragment(), LifecycleHandler, LifecycleHandlerDelegate {

  override val data: LifecycleHandlerData = LifecycleHandlerData(isAndroidXLifecycleHandler = true)

  override val routers: List<Router>
    get() = data.routerMap.values.toList()

  override val lifecycleActivity: Activity?
    get() = data.activity

  init {
    @Suppress("DEPRECATION")
    retainInstance = true
    setHasOptionsMenu(true)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleOnCreate(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    handleOnSaveInstanceState(outState)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    handleOnAttach(context)
  }

  @Suppress("DEPRECATION")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    handleOnActivityResult(requestCode, resultCode, data)
  }

  @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    handleOnRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  override fun shouldShowRequestPermissionRationale(permission: String): Boolean {
    return handleShouldShowRequestPermissionRationale(permission) {
      super.shouldShowRequestPermissionRationale(permission)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    handleOnCreateOptionsMenu(menu, inflater)
  }

  override fun onPrepareOptionsMenu(menu: Menu) {
    super.onPrepareOptionsMenu(menu)
    handleOnPrepareOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return handleOnOptionsItemSelected(item) {
      super.onOptionsItemSelected(item)
    }
  }

  override fun getRouter(container: ViewGroup, savedInstanceState: Bundle?): Router {
    return getRouter(container, savedInstanceState, this)
  }

  override fun registerActivityListener(activity: Activity) {
    handleRegisterActivityListener(activity, this)
  }

  override fun registerForActivityResult(instanceId: String, requestCode: Int) {
    handleRegisterForActivityResult(instanceId, requestCode)
  }

  override fun unregisterForActivityResults(instanceId: String) {
    handleUnregisterForActivityResults(instanceId)
  }

  override fun startActivityForResult(instanceId: String, intent: Intent, requestCode: Int, options: Bundle?) {
    handleStartActivityForResult(instanceId, intent, requestCode, options)
  }

  @Suppress("DEPRECATION")
  override fun startIntentSenderForResult(
    instanceId: String,
    intent: IntentSender,
    requestCode: Int,
    fillInIntent: Intent?,
    flagsMask: Int,
    flagsValues: Int,
    extraFlags: Int,
    options: Bundle?,
  ) {
    handleStartIntentSenderForResult(
      instanceId = instanceId,
      intent = intent,
      requestCode = requestCode,
      fillInIntent = fillInIntent,
      flagsMask = flagsMask,
      flagsValues = flagsValues,
      extraFlags = extraFlags,
      options = options,
    ) {
      startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options)
    }
  }

  override fun requestPermissions(instanceId: String, permissions: Array<String>, requestCode: Int) {
    handleRequestPermissions(instanceId, permissions, requestCode)
  }

  override fun onDetach() {
    super.onDetach()
    handleOnDetach()
  }

  override fun onDestroy() {
    super.onDestroy()
    handleOnDestroy()
  }
}

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
internal class PlatformLifecycleHandlerImpl : android.app.Fragment(), LifecycleHandler, LifecycleHandlerDelegate {

  override val data: LifecycleHandlerData = LifecycleHandlerData(isAndroidXLifecycleHandler = false)

  override val routers: List<Router>
    get() = data.routerMap.values.toList()

  override val lifecycleActivity: Activity?
    get() = data.activity

  init {
    retainInstance = true
    setHasOptionsMenu(true)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleOnCreate(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    handleOnSaveInstanceState(outState)
  }

  override fun onAttach(activity: Activity) {
    super.onAttach(activity)
    handleOnAttach(activity)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    handleOnAttach(context)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    handleOnActivityResult(requestCode, resultCode, data)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    handleOnRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  override fun shouldShowRequestPermissionRationale(permission: String): Boolean {
    return handleShouldShowRequestPermissionRationale(permission) {
      super.shouldShowRequestPermissionRationale(permission)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    handleOnCreateOptionsMenu(menu, inflater)
  }

  override fun onPrepareOptionsMenu(menu: Menu) {
    super.onPrepareOptionsMenu(menu)
    handleOnPrepareOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return handleOnOptionsItemSelected(item) {
      super.onOptionsItemSelected(item)
    }
  }

  override fun getRouter(container: ViewGroup, savedInstanceState: Bundle?): Router {
    return getRouter(container, savedInstanceState, this)
  }

  override fun registerActivityListener(activity: Activity) {
    handleRegisterActivityListener(activity, this)
  }

  override fun registerForActivityResult(instanceId: String, requestCode: Int) {
    handleRegisterForActivityResult(instanceId, requestCode)
  }

  override fun unregisterForActivityResults(instanceId: String) {
    handleUnregisterForActivityResults(instanceId)
  }

  override fun startActivityForResult(instanceId: String, intent: Intent, requestCode: Int, options: Bundle?) {
    handleStartActivityForResult(instanceId, intent, requestCode, options)
  }

  @RequiresApi(Build.VERSION_CODES.N)
  override fun startIntentSenderForResult(
    instanceId: String,
    intent: IntentSender,
    requestCode: Int,
    fillInIntent: Intent?,
    flagsMask: Int,
    flagsValues: Int,
    extraFlags: Int,
    options: Bundle?,
  ) {
    handleStartIntentSenderForResult(
      instanceId = instanceId,
      intent = intent,
      requestCode = requestCode,
      fillInIntent = fillInIntent,
      flagsMask = flagsMask,
      flagsValues = flagsValues,
      extraFlags = extraFlags,
      options = options,
    ) {
      startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options)
    }
  }

  override fun requestPermissions(instanceId: String, permissions: Array<String>, requestCode: Int) {
    handleRequestPermissions(instanceId, permissions, requestCode)
  }

  override fun onDetach() {
    super.onDetach()
    handleOnDetach()
  }

  override fun onDestroy() {
    super.onDestroy()
    handleOnDestroy()
  }
}

private interface LifecycleHandlerDelegate : ActivityLifecycleCallbacks {
  val data: LifecycleHandlerData

  private val routers: List<ActivityHostedRouter>
    get() = data.routerMap.values.toList()

  fun handleOnCreate(savedInstanceState: Bundle?) {
    savedInstanceState ?: return
    data.permissionRequestMap = savedInstanceState.parcelableCompat<StringSparseArrayParceler>(KEY_PERMISSION_REQUEST_CODES)
      ?.stringSparseArray
      ?: SparseArray()
    data.activityRequestMap = savedInstanceState.parcelableCompat<StringSparseArrayParceler>(KEY_ACTIVITY_REQUEST_CODES)
      ?.stringSparseArray
      ?: SparseArray()
    data.pendingPermissionRequests = savedInstanceState.parcelableArrayListCompat(KEY_PENDING_PERMISSION_REQUESTS)
      ?: ArrayList()
  }

  fun handleOnSaveInstanceState(outState: Bundle) {
    outState.putParcelable(KEY_PERMISSION_REQUEST_CODES, StringSparseArrayParceler(data.permissionRequestMap))
    outState.putParcelable(KEY_ACTIVITY_REQUEST_CODES, StringSparseArrayParceler(data.activityRequestMap))
    outState.putParcelableArrayList(KEY_PENDING_PERMISSION_REQUESTS, data.pendingPermissionRequests)
  }

  fun handleOnDestroy() {
    data.activity?.let { activity ->
      activity.application.unregisterActivityLifecycleCallbacks(this)
      activeLifecycleHandlers.remove(activity)
      destroyRouters(false)
      data.activity = null
    }

    data.routerMap.clear()
  }

  fun getRouter(container: ViewGroup, savedInstanceState: Bundle?, handler: LifecycleHandler): Router {
    data.routerMap[routerHashKey(container)]?.let {
      it.setHost(handler, container)
      return it
    }

    val router = ActivityHostedRouter()
    router.setHost(handler, container)
    savedInstanceState?.getBundle("$KEY_ROUTER_STATE_PREFIX${router.containerId}")?.let {
      router.restoreInstanceState(it)
    }
    data.routerMap[routerHashKey(container)] = router
    return router
  }

  fun handleRegisterActivityListener(activity: Activity, handler: LifecycleHandler) {
    data.activity = activity
    if (!data.hasRegisteredCallbacks) {
      data.hasRegisteredCallbacks = true
      activity.application.registerActivityLifecycleCallbacks(this)

      // Since Fragment transactions are async, we have to keep an <Activity, LifecycleHandler> map in addition
      // to trying to find the LifecycleHandler fragment in the Activity to handle the case of the developer
      // trying to immediately get > 1 router in the same Activity. See issue #299.
      activeLifecycleHandlers[activity] = handler
    }
  }

  fun requestPermissions(permissions: Array<String>, requestCode: Int)

  fun handleOnAttach(context: Context) {
    if (context is Activity) {
      data.activity = context
    }

    data.destroyed = false

    if (!data.attached) {
      data.attached = true

      for (i in data.pendingPermissionRequests.indices.reversed()) {
        val request = data.pendingPermissionRequests.removeAt(i)
        handleRequestPermissions(request.instanceId, request.permissions, request.requestCode)
      }

      routers.forEach { it.onContextAvailable() }
    }
  }

  fun handleOnDetach() {
    data.attached = false
    data.activity?.let { destroyRouters(it.isChangingConfigurations) }
  }

  private fun destroyRouters(configurationChange: Boolean) {
    if (!data.destroyed) {
      data.destroyed = true
      data.activity?.let { activity ->
        routers.forEach { it.onActivityDestroyed(activity, configurationChange) }
      }
    }
  }

  fun handleOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    this.data.activityRequestMap[requestCode]?.let { instanceId ->
      routers.forEach { it.onActivityResult(instanceId, requestCode, resultCode, data) }
    }
  }

  fun handleOnRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    data.permissionRequestMap[requestCode]?.let { instanceId ->
      routers.forEach { it.onRequestPermissionsResult(instanceId, requestCode, permissions, grantResults) }
    }
  }

  fun handleShouldShowRequestPermissionRationale(permission: String, callSuper: () -> Boolean): Boolean {
    for (router in routers) {
      val handled = router.handleRequestedPermission(permission)
      if (handled != null) {
        return handled
      }
    }

    return callSuper()
  }

  fun handleOnCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    routers.forEach { it.onCreateOptionsMenu(menu, inflater) }
  }

  fun handleOnPrepareOptionsMenu(menu: Menu) {
    routers.forEach { it.onPrepareOptionsMenu(menu) }
  }

  fun handleOnOptionsItemSelected(item: MenuItem, callSuper: () -> Boolean): Boolean {
    return routers.any { it.onOptionsItemSelected(item) } || callSuper()
  }

  fun handleRegisterForActivityResult(instanceId: String, requestCode: Int) {
    data.activityRequestMap.put(requestCode, instanceId)
  }

  fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?)

  fun handleStartActivityForResult(instanceId: String, intent: Intent, requestCode: Int, options: Bundle?) {
    handleRegisterForActivityResult(instanceId, requestCode)
    startActivityForResult(intent, requestCode, options)
  }

  fun handleStartIntentSenderForResult(
    instanceId: String,
    intent: IntentSender,
    requestCode: Int,
    fillInIntent: Intent?,
    flagsMask: Int,
    flagsValues: Int,
    extraFlags: Int,
    options: Bundle?,
    startIntentSender: () -> Unit,
  ) {
    handleRegisterForActivityResult(instanceId, requestCode)
    startIntentSender()
  }

  fun handleUnregisterForActivityResults(instanceId: String) {
    for (i in data.activityRequestMap.size() - 1 downTo 0) {
      if (instanceId == data.activityRequestMap[data.activityRequestMap.keyAt(i)]) {
        data.activityRequestMap.removeAt(i)
      }
    }
  }

  fun handleRequestPermissions(
    instanceId: String,
    permissions: Array<String>,
    requestCode: Int,
  ) {
    if (data.attached) {
      data.permissionRequestMap.put(requestCode, instanceId)
      requestPermissions(permissions, requestCode)
    } else {
      data.pendingPermissionRequests.add(PendingPermissionRequest(instanceId, permissions, requestCode))
    }
  }

  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    if (findInActivity(activity, data.isAndroidXLifecycleHandler) === this) {
      data.activity = activity
      data.routerMap.values.toList().forEach { it.onContextAvailable() }
    }
  }

  override fun onActivityStarted(activity: Activity) {
    if (data.activity === activity) {
      data.hasPreparedForHostDetach = false
      routers.forEach { it.onActivityStarted(activity) }
    }
  }

  override fun onActivityResumed(activity: Activity) {
    if (data.activity === activity) {
      routers.forEach { it.onActivityResumed(activity) }
    }
  }

  override fun onActivityPaused(activity: Activity) {
    if (data.activity === activity) {
      routers.forEach { it.onActivityPaused(activity) }
    }
  }

  override fun onActivityStopped(activity: Activity) {
    if (data.activity === activity) {
      prepareForHostDetachIfNeeded()
      routers.forEach { it.onActivityStopped(activity) }
    }
  }

  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    if (data.activity === activity) {
      prepareForHostDetachIfNeeded()

      routers.forEach {
        val bundle = Bundle()
        it.saveInstanceState(bundle)
        outState.putBundle("$KEY_ROUTER_STATE_PREFIX${it.containerId}", bundle)
      }
    }
  }

  override fun onActivityPreDestroyed(activity: Activity) {
    if (data.activity === activity && !activity.isChangingConfigurations) {
      handleOnDestroy()
    }
  }

  override fun onActivityDestroyed(activity: Activity) {
    activeLifecycleHandlers.remove(activity)
  }

  private fun prepareForHostDetachIfNeeded() {
    if (!data.hasPreparedForHostDetach) {
      data.hasPreparedForHostDetach = true
      routers.forEach { it.prepareForHostDetach() }
    }
  }
}

@Parcelize
internal class PendingPermissionRequest(
  val instanceId: String,
  val permissions: Array<String>,
  val requestCode: Int,
) : Parcelable

internal class LifecycleHandlerData(
  val isAndroidXLifecycleHandler: Boolean,
  var activity: Activity? = null,
  var hasRegisteredCallbacks: Boolean = false,
  var destroyed: Boolean = false,
  var attached: Boolean = false,
  var hasPreparedForHostDetach: Boolean = false,
  var permissionRequestMap: SparseArray<String> = SparseArray(),
  var activityRequestMap: SparseArray<String> = SparseArray(),
  var pendingPermissionRequests: ArrayList<PendingPermissionRequest> = arrayListOf(),
  val routerMap: MutableMap<Int, ActivityHostedRouter> = mutableMapOf(),
)

private fun findInActivity(activity: Activity, allowAndroidXBacking: Boolean): LifecycleHandler? {
  var lifecycleHandler = activeLifecycleHandlers[activity]
  if (lifecycleHandler == null) {
    lifecycleHandler = if (allowAndroidXBacking && activity is FragmentActivity) {
      activity.supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as? LifecycleHandler
    } else {
      @Suppress("DEPRECATION")
      activity.fragmentManager.findFragmentByTag(FRAGMENT_TAG) as? LifecycleHandler
    }
  }
  lifecycleHandler?.registerActivityListener(activity)
  return lifecycleHandler
}

private fun routerHashKey(viewGroup: ViewGroup) = viewGroup.id

private val activeLifecycleHandlers = mutableMapOf<Activity, LifecycleHandler>()

private const val FRAGMENT_TAG = "LifecycleHandler"
private const val KEY_PENDING_PERMISSION_REQUESTS = "LifecycleHandler.pendingPermissionRequests"
private const val KEY_PERMISSION_REQUEST_CODES = "LifecycleHandler.permissionRequests"
private const val KEY_ACTIVITY_REQUEST_CODES = "LifecycleHandler.activityRequests"
private const val KEY_ROUTER_STATE_PREFIX = "LifecycleHandler.routerState"
