using UnityEngine;
using System.Collections;

public class SetupUndistortion : MonoBehaviour {

	// enable GVR_style undistortion
    // use Awake because it runs before Start
	void Awake () {
        Shader.EnableKeyword("GVR_DISTORTION");
	}
}
