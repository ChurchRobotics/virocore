/*
 * Copyright © 2017 Viro Media. All rights reserved.
 */
package com.viro.renderer.jni;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ARNode is a specialized {@link Node} that corresponds to a detected {@link ARAnchor}. ARNodes are
 * automatically created by Viro and added to the {@link Scene} as they are detected, and by default
 * hold no content and have no children. Each ARNode is continually updated to stay in sync with its
 * corresponding ARAnchor: if the anchor's position, orientation, or other detected properties
 * change, the ARNode will be changed as well.
 * <p>
 * ARNode is the mechanism through which you can attach virtual content to real-world objects. For
 * example, if an {@link ARPlaneAnchor} is detected, you can add a 3D model to that plane by loading
 * the {@link Object3D} and making it a child of the ARNode.
 * <p>
 * To get an ARNode, attach a {@link com.viro.renderer.jni.ARScene.Delegate} to the {@link ARScene},
 * and listen for {@link com.viro.renderer.jni.ARScene.Delegate#onAnchorFound(ARAnchor, ARNode)},
 * which is invoked each time a new {@link ARAnchor} is found, with its corresponding {@link
 * ARNode}.
 */
public class ARNode extends Node {

    /**
     * ARNodes need strong references to native created ARNodes since their Java representation
     * typically isn't stored anywhere. We remove ARNodes from this map as they are removed by the
     * AR tracking engine.
     *
     * @hide
     */
    private static ConcurrentHashMap<Integer, ARNode> nodeARMap = new ConcurrentHashMap<Integer, ARNode>();

    /**
     * @hide
     */
    static ARNode getARNodeWithID(int id) {
        return nodeARMap.get(id);
    }

    /**
     * @hide
     */
    static ARNode removeARNodeWithID(int id) {
        return nodeARMap.remove(id);
    }

    /**
     * Protected constructor for subclasses (declarative nodes).
     * @hide
     */
    ARNode() {

    }

    /**
     * Protected constructor to wrap an ARNode created natively.
     * @hide
     * @param nativeRef
     */
    ARNode(long nativeRef) {
        super(false); // call the empty NodeJni constructor.
        setNativeRef(nativeRef);
        nodeARMap.put(nativeGetUniqueIdentifier(mNativeRef), this);
    }

    @Override
    protected void finalize() throws Throwable {
        try {

        } finally {
            super.finalize();
        }
    }

    /**
     * Set to true to pause automatic synchronization between this ARNode and its {@link ARAnchor}.
     * ARAnchors are periodically updated by the AR tracking system as its estimates of the anchor's
     * properties are refined. By default, updates to the ARAnchor are synchronized to the ARNode;
     * for example, if the tracking system determines that an ARAnchor has moved, the ARNode will
     * move as well.
     * <p>
     * It may be useful to pause updates if you wish to ensure the stability of your {@link Scene}
     * for a period of time. The ARNode will be immediately updated to its anchor's latest position
     * when pause updates is turned off.
     *
     * @param pauseUpdates True to pause updates, false to resume updating. When set to false, the
     *                     ARNode will immediately be updated to match its {@link ARAnchor}.
     */
    public void setPauseUpdates(boolean pauseUpdates) {
        nativeSetPauseUpdates(mNativeRef, pauseUpdates);
    }

    private native void nativeSetPauseUpdates(long nativeRef, boolean pauseUpdates);

}
