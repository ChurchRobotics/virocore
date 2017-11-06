/**
 * Copyright © 2017 Viro Media. All rights reserved.
 */
package com.viro.renderer.jni;

/**
 * ARScene blends virtual 3D content with the device camera's view of the real world. Similar to a
 * normal {@link Scene}, an ARScene contains a hierarchy of {@link Node} objects representing the
 * virtual 3D world. Behind these objects the camera's live video feed is rendered. ARScene ensures
 * the two worlds -- real and virtual -- stay in sync with a unified coordinate system.
 * <p>
 * There are two ways to add virtual content to the ARScene. The first is to simply place content
 * manually. The origin of the coordinate system is the <i>initial</i> position of the user, and the
 * scale is meters. To add content you only need to set the position of the {@link Node} within this
 * coordinate system, and add it to the scene graph.
 * <p>
 * The second way to add virtual content is to use {@link ARAnchor}. ARAnchors represent features
 * detected in the real-world. You can associate (or "anchor") your content to these features by
 * adding your content to the {@link ARNode} that corresponds to each detected ARAnchor. To do this,
 * implement the ARScene {@link Delegate}. The {@link Delegate} callbacks are invoked whenever
 * {@link ARAnchor}s are found (or updated, or removed) in the real-world.
 */
public class ARScene extends Scene {

    /**
     * Callback interface for ARScene events. These include the detection of ambient light and the
     * tracking of real-world features.
     */
    public interface Delegate {
        /**
         * Invoked when tracking is initialized and functional.
         */
        void onTrackingInitialized();

        /**
         * Invoked when the AR system's estimation of the current ambient light levels are
         * updated. These values can be pumped into an {@link AmbientLight} to match the
         * lighting used by the virtual world to the lighting observed in the real world.
         *
         * @param lightIntensity   The light intensity detected.
         * @param colorTemperature The color temperature detected.
         */
        void onAmbientLightUpdate(float lightIntensity, float colorTemperature);

        /**
         * Invoked when a real-world {@link ARAnchor} is detected. You can associate virtual
         * content with this new anchor by adding said content to the associated {@link ARNode}.
         * Note that the {@link ARNode} is automatically added to the Scene, and will be
         * continually updated to stay in the sync with the anchor as the anchor's properties,
         * orientation, or position change.
         *
         * @param anchor The detected {@link ARAnchor} representing a real-world feature.
         * @param node   The virtual world {@link ARNode} associated with the ARAnchor.
         */
        void onAnchorFound(ARAnchor anchor, ARNode node);

        /**
         * Invoked when a real-world {@link ARAnchor} is updated. This occurs when the AR tracking
         * system refines its estimation about the size or orientation or position of an ARAnchor,
         * or when other underlying properties of the anchor change.
         *
         * @param anchor The {@link ARAnchor} that was updated.
         * @param node   The {@link ARNode} corresponding to the anchor.
         */
        void onAnchorUpdated(ARAnchor anchor, ARNode node);

        /**
         * Invoked when an {@link ARAnchor} is removed from the world. This occurs if the AR
         * tracking system loses confidence in the ARAnchor and can no longer place it, or when an
         * ARAnchor gets subsumed by another: for example, if the tracking system realizes that two
         * separate horizontal planes are actually one larger plane.
         *
         * @param anchor The {@link ARAnchor} that was removed.
         * @param node   The corresponding {@link ARNode}, which is also removed from the Scene.
         */
        void onAnchorRemoved(ARAnchor anchor, ARNode node);
    }

    private Delegate mDelegate = null;
    private long mNativeARDelegateRef;

    /**
     * Construct a new ARScene.
     */
    public ARScene() {
        mNativeARDelegateRef = nativeCreateARSceneDelegate(mNativeRef);
    }

    /**
     * Constructor for the declarative session.
     *
     * @hide
     * @param declarative
     */
    public ARScene(boolean declarative) {
        super(true); // Invoke the dummy constructor
        long nativeRef = nativeCreateARSceneControllerDeclarative();
        setSceneRef(nativeRef);
        mNativeARDelegateRef = nativeCreateARSceneDelegate(mNativeRef);
    }

    /**
     * Invoked only by the default (no-arg) constructor.
     *
     * @hide
     */
    @Override
    protected long createNativeScene() {
        return nativeCreateARSceneController();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            dispose();
        } finally {
            super.finalize();
        }
    }

    /**
     * Release native resources associated with this ARScene.
     */
    @Override
    public void dispose() {
        super.dispose();
        if (mNativeARDelegateRef != 0) {
            nativeDestroyARSceneDelegate(mNativeARDelegateRef);
            mNativeARDelegateRef = 0;
        }
    }

    /**
     * Set to true to display a point cloud representing the rough feature points that the AR
     * tracking system is detecting. This is useful for debugging and analysis, but can also be
     * used for rendering effects.
     *
     * @param displayPointCloud True to display the point cloud, false to remove.
     */
    public void displayPointCloud(boolean displayPointCloud) {
        nativeDisplayPointCloud(mNativeRef, displayPointCloud);
    }

    /**
     * Reset the point cloud surface to the default values. The point cloud surface is the
     * {@link Surface} that will be used to render each point in the point cloud.
     */
    public void resetPointCloudSurface() {
        nativeResetPointCloudSurface(mNativeRef);
    }

    /**
     * Set the {@link Surface} that should be used to render each point in the rendered
     * AR point cloud. This allows you to customize the appearance of the cloud with a
     * textured (or otherwise colored) Surface.
     *
     * @param surface The {@link Surface} to use for representing each point in the cloud.
     */
    public void setPointCloudSurface(Surface surface) {
        nativeSetPointCloudSurface(mNativeRef, surface.mNativeRef);
    }

    /**
     * Set the scale factor to use when rendering each point in the AR point cloud.
     *
     * @param scale The scale as a {@link Vector} in X, Y, and Z dimensions.
     */
    public void setPointCloudSurfaceScale(Vector scale) {
        nativeSetPointCloudSurfaceScale(mNativeRef, scale.x, scale.y, scale.z);
    }

    /**
     * Set the maximum number of points to render when drawing the AR point cloud.
     *
     * @param maxPoints The maximum number of points to render.
     */
    public void setPointCloudMaxPoints(int maxPoints) {
        nativeSetPointCloudMaxPoints(mNativeRef, maxPoints);
    }

    /**
     * @hide
     */
    public void addARDeclarativeNode(ARDeclarativeNode node) {
        nativeAddARNode(mNativeRef, node.mNativeRef);
    }

    /**
     * @hide
     */
    public void updateARDeclarativeNode(ARDeclarativeNode node) {
        nativeUpdateARNode(mNativeRef, node.mNativeRef);
    }
    /**
     * @hide
     */
    public void removeARDeclarativeNode(ARDeclarativeNode node) {
        nativeRemoveARNode(mNativeRef, node.mNativeRef);
    }

    private native long nativeCreateARSceneController();
    private native long nativeCreateARSceneControllerDeclarative();
    private native long nativeCreateARSceneDelegate(long sceneControllerRef);
    private native void nativeDestroyARSceneDelegate(long delegateRef);
    private native void nativeAddARNode(long sceneControllerRef, long arPlaneRef);
    private native void nativeUpdateARNode(long sceneControllerRef, long arPlaneRef);
    private native void nativeRemoveARNode(long sceneControllerRef, long arPlaneRef);
    private native void nativeDisplayPointCloud(long sceneControllerRef, boolean displayPointCloud);
    private native void nativeResetPointCloudSurface(long sceneControllerRef);
    private native void nativeSetPointCloudSurface(long sceneControllerRef, long surfaceRef);
    private native void nativeSetPointCloudSurfaceScale(long sceneControllerRef, float scaleX, float scaleY, float scaleZ);
    private native void nativeSetPointCloudMaxPoints(long sceneControllerRef, int maxPoints);

    public void setDelegate(Delegate delegate) {
        mDelegate = delegate;
    }

    // Called by JNI

    /**
     * @hide
     */
    void onTrackingInitialized() {
        if (mDelegate != null) {
            mDelegate.onTrackingInitialized();
        }
    }
    /**
     * @hide
     */
    void onAmbientLightUpdate(float lightIntensity, float colorTemperature) {
        Delegate delegate;
        if (mDelegate != null) {
            mDelegate.onAmbientLightUpdate(lightIntensity, colorTemperature);
        }
    }
    /**
     * @hide
     */
    void onAnchorFound(ARAnchor anchor, long nodeNativeRef) {
        if (mDelegate != null) {
            ARNode node = null;
            if (nodeNativeRef != 0) {
                node = new ARNode(nodeNativeRef);
            }
            mDelegate.onAnchorFound(anchor, node);
        }
    }
    /**
     * @hide
     */
    void onAnchorUpdated(ARAnchor anchor, int nodeId) {
        if (mDelegate != null) {
            ARNode node = null;
            if (nodeId != 0) {
                node = ARNode.getARNodeWithID(nodeId);
            }
            mDelegate.onAnchorUpdated(anchor, node);
        }
    }
    /**
     * @hide
     */
    /* Called by Native */
    void onAnchorRemoved(ARAnchor anchor, int nodeId) {
        if (mDelegate != null) {
            ARNode node = null;
            if (nodeId != 0) {
                node = ARNode.removeARNodeWithID(nodeId);
            }
            mDelegate.onAnchorRemoved(anchor, node);
        }
    }
}
