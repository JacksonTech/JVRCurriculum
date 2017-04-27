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

public class SoundTarget : MonoBehaviour {
    // reference to gvr audio source
    public AudioSource sound;

    public void ClickMe()
    {
        // move somewhere
        float x = Random.Range(-10, 10);
        float z = Random.Range(-10, 10);

        // get old 
        Vector3 v = transform.position;

        // set new
        v.x = x;
        v.z = z;

        // assign back
        transform.position = v;
        // play success sound
        sound.Play();
    }
}
