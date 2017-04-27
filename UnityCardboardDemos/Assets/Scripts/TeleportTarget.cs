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
using System;
using UnityEngine.EventSystems;

public class TeleportTarget : MonoBehaviour
{
    public Camera cam;

    // when the move and unblink should occur
    private float timeToMove;

    // how long should we wait before moving after the fade starts
    private float moveDelay = 0.4f;

    // are we in the process of moving?
    private bool moving;

    // call this from your scripts to move
    public void moveMe()
    {
        if (moving) return;

        timeToMove = Time.time + moveDelay;
        SimpleFader.BeginFadeOut();
        moving = true;
    }

    // wait for proper time to move
    void Update()
    {
        if (moving && Time.time > timeToMove)
        {
            cam.transform.position = transform.position;
            SimpleFader.BeginFadeIn();
            moving = false;
        }
    }
}

