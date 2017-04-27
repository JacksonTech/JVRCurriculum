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
using VRStandardAssets.Utils;

public class ButtonScriptWithSound : MonoBehaviour {

    public SelectionRadial selectionRadial;
    public VRInteractiveItem interactiveItem;
    public DoorScriptWithSound doorScript;

    private bool lookingAt;

    void OnEnable()
    {
        selectionRadial.OnSelectionComplete += clickMe;
        interactiveItem.OnOver += HandleOver;
        interactiveItem.OnOut += HandleOut;
    }

    void OnDisable()
    {
        selectionRadial.OnSelectionComplete -= clickMe;
        interactiveItem.OnOver -= HandleOver;
        interactiveItem.OnOut -= HandleOut;
    }

    void HandleOver()
    {
        selectionRadial.Show();
        lookingAt = true;
    }

    void HandleOut()
    {
        selectionRadial.Hide();
        lookingAt = false;
    }

    public void clickMe()
    {
        if (!lookingAt) return;

        doorScript.Open();
    }
}
