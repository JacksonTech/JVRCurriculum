using UnityEngine;
using System.Collections;

public class SunScript : MonoBehaviour {
    public GameObject sun;
    public GameObject planet;

	// Use this for initialization
	void Start () {
	
	}
	
	// Update is called once per frame
	void Update () {
        transform.Rotate(new Vector3(0, 0, 1) * Time.deltaTime * 4);
        sun.transform.Rotate(new Vector3(0, 0, -1) * Time.deltaTime * 4);
        planet.transform.Rotate(new Vector3(0, 0, 2) * Time.deltaTime * 4);
	}
}
