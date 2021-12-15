package com.kocsistem.arcore_sceneform

import android.app.Activity
import android.app.ActivityManager
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.kocsistem.arcore_sceneform.R
import android.view.Gravity

import com.google.ar.sceneform.assets.RenderableSource
import android.view.MotionEvent
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.TransformableNode
import java.lang.ref.WeakReference
import java.util.function.Consumer
import java.util.function.Function


class ARActivity : AppCompatActivity()  {
    private val GLTF_ASSET =
        "https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF/Duck.gltf"
    private val MIN_OPENGL_VERSION = 3.0
    private lateinit var arFragment: ArFragment
    private lateinit var renderable: ModelRenderable

    @Override
    @SuppressWarnings("AndroidApiChecker", "FutureReturnValueIgnored")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        val weakActivity: WeakReference<ARActivity> = WeakReference(this)
        /*
        ModelRenderable.builder().
        setSource(
            this,
            Uri.parse(
                "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb"
            )

        )*/
        ModelRenderable.builder()
            .setSource(this, R.raw.andy)
            .build()
            .thenAccept(Consumer { modelRenderable: ModelRenderable ->
                renderable = modelRenderable
            })
            .exceptionally(
                Function<Throwable, Void?> { throwable: Throwable? ->
                    val toast =
                        Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    null
                })
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            if (renderable == null) {
                return@setOnTapArPlaneListener
            }
            // Create the Anchor.
            val anchor = hitResult.createAnchor()
            val anchorNode =
                AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)
            // Create the transformable andy and add it to the anchor.
            val andy =
                TransformableNode(arFragment.transformationSystem)
            andy.setParent(anchorNode)
            andy.renderable = renderable
            andy.select()
        }
        /*
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            if (renderable == null) {
                return@setOnTapArPlaneListener
            }

            // Create the Anchor.
            val anchor: Anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            // Create the transformable andy and add it to the anchor.
            val andy =
                TransformableNode(arFragment.transformationSystem)
            andy.setParent(anchorNode)
            andy.setRenderable(renderable)
            andy.select()
        }
        */
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        val openGlVersionString: String =
            (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .getDeviceConfigurationInfo()
                .getGlEsVersion()
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }
}