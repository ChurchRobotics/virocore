/**
 * Copyright (c) 2015-present, ViroMedia, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.viro.renderer;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.viro.renderer.jni.BoxJni;
import com.viro.renderer.jni.ImageJni;
import com.viro.renderer.jni.MaterialJni;
import com.viro.renderer.jni.NodeJni;
import com.viro.renderer.jni.SceneJni;
import com.viro.renderer.jni.SphereJni;
import com.viro.renderer.jni.SurfaceJni;
import com.viro.renderer.jni.TextureJni;
import com.viro.renderer.jni.VideoTextureJni;
import com.viro.renderer.jni.ViroGvrLayout;
import com.viro.renderer.jni.VrView;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViroActivity extends AppCompatActivity {
    private VrView mVrView;
    private static String TAG = ViroActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVrView = new ViroGvrLayout(this);
        mVrView.setVrModeEnabled(true);
        setContentView((ViroGvrLayout) mVrView);
    }

    @Override
    protected void onStart(){
        super.onStart();

        mVrView.getNativeRenderer().enableReticle(false);

        // Creation of SceneJni within scene navigator
        NodeJni rootNode = new NodeJni(this);
        SceneJni scene = new SceneJni(rootNode);

        List<NodeJni> nodes = new ArrayList<>();
        //nodes = testSurfaceVideo(this);
        //nodes = testSphereVideo(this);
        //nodes = testBox(this);
        //nodes = testImageSurface(this);

        //testBackgroundVideo(scene);
        testBackgroundImage(scene);

        for (NodeJni node: nodes) {
            rootNode.addChildNode(node);
        }

        // Updating the scene.
        mVrView.setScene(scene);
    }

    private List<NodeJni> testSurfaceVideo(Context context) {
        NodeJni node = new NodeJni(context);
        SurfaceJni surface = new SurfaceJni(4,4);
        float[] position = {0,0,-3};
        node.setPosition(position);
        VideoTextureJni videoTexture = new VideoTextureJni();
        videoTexture.loadSource("https://s3.amazonaws.com/viro.video/Climber2Top.mp4", mVrView.getRenderContextRef());
        videoTexture.setVolume(0.1f);
        videoTexture.setLoop(false);
        videoTexture.play();
        videoTexture.setVideoDelegate(new VideoTextureJni.VideoDelegate() {
            @Override
            public void onVideoFinish() {
                Log.e(TAG,"onVideoFinished for Surface within ViroActivity");
            }
        });
        surface.setVideoTexture(videoTexture);
        node.setGeometry(surface);
        return Arrays.asList(node);
    }

    private List<NodeJni> testSphereVideo(Context context) {
        NodeJni node = new NodeJni(context);
        SphereJni sphere = new SphereJni(2, 20, 20, false);
        VideoTextureJni videoTexture = new VideoTextureJni();
        videoTexture.loadSource("https://s3.amazonaws.com/viro.video/Climber2Top.mp4", mVrView.getRenderContextRef());
        videoTexture.setVolume(0.1f);
        videoTexture.setLoop(false);
        videoTexture.play();
        videoTexture.setVideoDelegate(new VideoTextureJni.VideoDelegate() {
            @Override
            public void onVideoFinish() {
                Log.e(TAG,"onVideoFinished for Sphere within ViroActivity");
            }
        });
        sphere.setVideoTexture(videoTexture);
        node.setGeometry(sphere);
        return Arrays.asList(node);
    }

    private void testBackgroundVideo(SceneJni scene) {
        VideoTextureJni videoTexture = new VideoTextureJni();
        videoTexture.loadSource("https://s3.amazonaws.com/viro.video/Climber2Top.mp4", mVrView.getRenderContextRef());
        videoTexture.setVolume(0.1f);
        videoTexture.setLoop(false);
        videoTexture.play();
        videoTexture.setVideoDelegate(new VideoTextureJni.VideoDelegate() {
            @Override
            public void onVideoFinish() {
                Log.e(TAG,"onVideoFinished for Background within ViroActivity");
            }
        });
        scene.setBackgroundVideoTexture(videoTexture);
    }

    private void testBackgroundImage(SceneJni scene) {
        ImageJni imageJni = new ImageJni("boba.png");
        TextureJni videoTexture = new TextureJni(imageJni);
        scene.setBackgroundImageTexture(videoTexture);
        float[] rotation = {90, 0, 0};
        scene.setBackgroundRotation(rotation);
    }

    private List<NodeJni> testBox(Context context) {
        NodeJni node1 = new NodeJni(context);
        NodeJni node2 = new NodeJni(context);
        // Create a new material with a diffuseTexture set to the image "boba.png"
        ImageJni bobaImage = new ImageJni("boba.png");

        TextureJni bobaTexture = new TextureJni(bobaImage);
        MaterialJni material = new MaterialJni();
        material.setTexture(bobaTexture, "diffuseTexture");

        // Creation of ViroBox to the right and billboarded
        BoxJni boxGeometry = new BoxJni(2,4,2);
        node1.setGeometry(boxGeometry);
        float[] boxPosition = {5,0,-3};
        node1.setPosition(boxPosition);
        node1.setMaterials(Arrays.asList(material));
        String[] behaviors = {"billboard"};
        node1.setTransformBehaviors(behaviors);

        // Creation of ViroBox to the left not billboarded
        BoxJni boxGeometry2 = new BoxJni(2, 2, 2);
        node2.setGeometry(boxGeometry2);
        float[] boxPosition2 = {-2, 0, -3};
        node2.setPosition(boxPosition2);
        node2.setMaterials(Arrays.asList(material));
        return Arrays.asList(node1, node2);
    }

    private List<NodeJni> testImageSurface(Context context) {
        NodeJni node = new NodeJni(context);
        ImageJni bobaImage = new ImageJni("boba.png");

        TextureJni bobaTexture = new TextureJni(bobaImage);
        MaterialJni material = new MaterialJni();

        SurfaceJni surface = new SurfaceJni(1, 1);
        surface.setMaterial(material);
        surface.setImageTexture(bobaTexture);

        node.setGeometry(surface);
        float[] position = {0, 0, -2};
        node.setPosition(position);
        return Arrays.asList(node);
    }
}
