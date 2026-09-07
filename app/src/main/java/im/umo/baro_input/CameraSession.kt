package im.umo.baro_input

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

internal class CameraSession(private val context: Context) : LifecycleOwner {
    private val registry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = registry
    private var provider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var capture: ImageCapture? = null
    private var camera: Camera? = null
    private var generation = 0
    init { registry.currentState = Lifecycle.State.CREATED }

    fun start(view: PreviewView, ready: (Boolean) -> Unit, error: () -> Unit) {
        stop()
        val version = generation
        registry.currentState = Lifecycle.State.STARTED
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (version != generation) return@addListener
            try {
                val p = future.get(); provider = p
                val lens = if (p.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
                val previewUseCase = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
                val imageUseCase = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
                preview = previewUseCase; capture = imageUseCase
                camera = p.bindToLifecycle(this, lens, previewUseCase, imageUseCase)
                ready(camera?.cameraInfo?.hasFlashUnit() == true)
            } catch (_: Exception) { stop(); error() }
        }, ContextCompat.getMainExecutor(context))
    }

    fun torch(on: Boolean) { camera?.cameraControl?.enableTorch(on) }

    fun photograph(done: (Bitmap) -> Unit, error: () -> Unit) {
        val shot = capture ?: return error()
        val version = generation
        shot.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    if (version != generation) return
                    val original = image.toBitmap()
                    val degrees = image.imageInfo.rotationDegrees
                    val bitmap = if (degrees == 0) original else Bitmap.createBitmap(original, 0, 0, original.width, original.height, Matrix().apply { postRotate(degrees.toFloat()) }, true)
                    if (bitmap !== original) original.recycle()
                    done(bitmap)
                } catch (_: Exception) { if (version == generation) error() }
                finally { image.close() }
            }
            override fun onError(exception: ImageCaptureException) { if (version == generation) error() }
        })
    }

    fun stop() {
        generation++
        camera?.cameraControl?.enableTorch(false)
        val useCases = listOfNotNull(preview, capture)
        if (useCases.isNotEmpty()) provider?.unbind(*useCases.toTypedArray())
        preview = null; capture = null; camera = null
        registry.currentState = Lifecycle.State.CREATED
    }
    fun close() { stop(); registry.currentState = Lifecycle.State.DESTROYED }
}
