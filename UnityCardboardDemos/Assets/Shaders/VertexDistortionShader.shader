﻿/*
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

Shader "Demo/VertexDistortionShader"
{
	Properties
	{
		_Color ("Main Color", Color) = (1, 1, 1, 1)
	}
	SubShader
	{
		Tags { "RenderType"="Opaque" }
		LOD 100

		Pass
		{
			CGPROGRAM
			#pragma vertex vert
			#pragma fragment frag
			// make fog work
			#pragma multi_compile_fog
			
			#include "UnityCG.cginc"
			
			// include the undistortion from GVR SDK
			// this keyword is set in a script
			#pragma multi_compile __ GVR_DISTORTION
			#include "GvrDistortion.cginc"

			// incoming data from app
			struct appdata
			{
				float4 vertex : POSITION;
				float2 lightmapUv : TEXCOORD1; // light data comes in on uv channel 1
			};

			// struct to hold data going from vert stage -> frag stage
			struct v2f
			{
				float2 lightmapUv : TEXCOORD1;
				UNITY_FOG_COORDS(1)
				float4 vertex : SV_POSITION;
			};
			
			// from properties
			float4 _Color;

			v2f vert (appdata v)
			{
				v2f o;
				o.vertex = undistortVertex(v.vertex);
				// select the right part of the lightmap for the object
				o.lightmapUv = v.lightmapUv * unity_LightmapST.xy + unity_LightmapST.zw;
				UNITY_TRANSFER_FOG(o,o.vertex); // do fog...stuff
				return o;
			}
			
			fixed4 frag (v2f i) : SV_Target
			{
				fixed4 col = _Color;
				col.rgb *= DecodeLightmap(UNITY_SAMPLE_TEX2D(unity_Lightmap, i.lightmapUv));
				UNITY_APPLY_FOG(i.fogCoord, col);
				return col;
			}
			ENDCG
		}
	}
}
