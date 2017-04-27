/*
Copyright 2016 Cody Jackson

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

using UnityEngine;
using System.Collections;

// add this component to your Camera.
// then call SimpleFader.BeginFadeIn() and SimpleFader.BeginFadeOut() as
// appropriate from your scripts
public class SimpleFader : MonoBehaviour {

    // based on http://answers.unity3d.com/questions/341350/how-to-fade-out-a-scene.html
    // by Griffo

    private Texture2D black;

    // time to completely fade in/out
    public float fadeSpeed = 1.3f;
    public int drawDepth = -1000;

    private float alpha = 1f;

    // -1 to fade in, 1 to fade out
    private float fadeDir = -1;
    private Rect rect;

    // for singleton
    public static SimpleFader instance;

    // Gets called whenever the GUI is drawn.
    // Draws the black texture across the screen in front of everything else
    void OnGUI()
    {
        alpha += fadeDir * fadeSpeed * Time.deltaTime;
        alpha = Mathf.Clamp01(alpha);
        Color curColor = GUI.color;
        curColor.a = alpha;
        GUI.color = curColor;
        GUI.depth = drawDepth;
        GUI.DrawTexture(new Rect(0, 0, Screen.width, Screen.height), black);
    }

	// Use this for initialization
	void Start () {
        black = new Texture2D(1, 1);
        black.SetPixel(0, 0, Color.black);
        black.Apply();
	}

    // call this to fade out
    public static void BeginFadeOut()
    {
        getInstance();
        if (instance != null)
        {
            instance.FadeOut();
        }
       
    }

    // call this to fade in
    public static void BeginFadeIn()
    {
        getInstance();
        if (instance != null)
        {
            instance.FadeIn();
        }
    }

    public void FadeOut()
    {
        fadeDir = 1;
    }

    public void FadeIn()
    {
        fadeDir = -1;
    }

    public static SimpleFader getInstance()
    {
        if (instance == null)
        {
            instance = GameObject.FindObjectOfType<SimpleFader>();
        }
        return instance;
    }
}
