using UnityEngine;
using System.Collections;

public class PlanetScript : MonoBehaviour {
    public GameObject planet, moon;

	// Use this for initialization
	void Start () {
	
	}
	
	// Update is called once per frame
	void Update () {
        transform.Rotate(new Vector3(-1, 0, 0) * Time.deltaTime * 4);
        planet.transform.Rotate(new Vector3(1, 0, 0) * Time.deltaTime * 4);
        moon.transform.Rotate(new Vector3(0, 0, -1) * Time.deltaTime * 4);
	}
}
