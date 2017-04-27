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
using UnityEngine.UI;
using System.Collections;

public class WalkCanvas : MonoBehaviour {
    public CharacterController playerController;
    public Transform cameraTransform;

    public float speed = 2f;
    public bool isWalking;

    void LateUpdate()
    {
        // copy only yaw
        Vector3 angles = cameraTransform.rotation.eulerAngles;
        transform.rotation = Quaternion.Euler(55, angles.y, 0);

        // copy position...
        transform.position = cameraTransform.position +
            // ... move 1.5 in front of camera
            // uses camera transform to move Vector3.forward into world space
            transform.TransformDirection(Vector3.forward) * 1.5f -
            //  ... and a bit down
            (0.9f * Vector3.up);

        if (isWalking)
        {
            playerController.SimpleMove(cameraTransform.TransformDirection(Vector3.forward) * speed);
        }
    }

    public void clickMe()
    {
        isWalking = !isWalking;
    }
}
