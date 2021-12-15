package com.kocsistem.arcore_sceneform

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.ArraySet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Toast
//import com.google.android.filament.gltfio.Animator

import com.google.ar.sceneform.ux.ArFragment
import java.lang.ref.WeakReference
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

import com.google.ar.sceneform.math.Vector3

//import com.google.android.filament.gltfio.FilamentAsset
import com.google.ar.core.Anchor

import com.google.ar.sceneform.ux.TransformableNode

import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene.OnUpdateListener
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.BaseArFragment.OnTapArPlaneListener
import java.lang.AssertionError
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private val GLTF_ASSET =
        "https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF/Duck.gltf"

    private val TAG: String = MainActivity::class.java.getSimpleName()
    private val MIN_OPENGL_VERSION = 3.0

    private var arFragment: ArFragment? = null
    private var renderable: ModelRenderable? = null

    //private var animators: Set<AnimationInstance> = ArraySet()

    private val colors: List<Color> = Arrays.asList(
        Color(0F, 0F, 0F, 1F),
        Color(1F, 0F, 0F, 1F),
        Color(0F, 1F, 0F, 1F),
        Color(0F, 0F, 1F, 1F),
        Color(1F, 1F, 0F, 1F),
        Color(0F, 1F, 1F, 1F),
        Color(1F, 0F, 1F, 1F),
        Color(1F, 1F, 1F, 1F)
    )
    private var nextColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
        val weakActivity: WeakReference<MainActivity> = WeakReference(this)
        //https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF/Duck.gltf
        //https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb
        ModelRenderable.builder()
            .setSource(
                this,
                Uri.parse(
                    "https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF/Duck.gltf"
                )

            )
           // .setIsFilamentGltf(true)
            .build()
            .thenAccept(
                Consumer { modelRenderable: ModelRenderable ->
                    val activity: MainActivity? = weakActivity.get()
                    if (activity != null) {
                        activity.renderable = modelRenderable
                    }
                })
            .exceptionally(
                Function<Throwable, Void?> { throwable: Throwable? ->
                    val toast = Toast.makeText(
                        this,
                        "Unable to load Tiger renderable",
                        Toast.LENGTH_LONG
                    )
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    null
                })


        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            if (renderable == null) {
                return@setOnTapArPlaneListener
            }

            // Create the Anchor.
            val anchor: Anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment!!.arSceneView.scene)

            // Create the transformable model and add it to the anchor.
            val model =
                TransformableNode(arFragment!!.transformationSystem)
            model.setParent(anchorNode)
            model.renderable = renderable
            model.select()

            val color = colors[nextColor]
            nextColor++
            for (i in 0 until renderable!!.submeshCount) {
                val material: Material = renderable!!.getMaterial(i)
                material.setFloat4("baseColorFactor", color)
            }
            val tigerTitleNode = Node()
            tigerTitleNode.setParent(model)
            tigerTitleNode.setEnabled(false)
            tigerTitleNode.setLocalPosition(Vector3(0.0f, 1.0f, 0.0f))
            ViewRenderable.builder()
                .setView(this, R.layout.tiger_card_view)
                .build()
                .thenAccept { renderable: ViewRenderable? ->
                    tigerTitleNode.setRenderable(renderable)
                    tigerTitleNode.setEnabled(true)
                }
                .exceptionally { throwable: Throwable? ->
                    throw AssertionError(
                        "Could not load card view.",
                        throwable
                    )
                }
        }
    }




    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     *
     * Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     *
     * Finishes the activity if Sceneform can not run
     */
    fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
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
