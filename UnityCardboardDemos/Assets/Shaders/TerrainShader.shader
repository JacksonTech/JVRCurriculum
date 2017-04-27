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

Shader "Demo/TerrainShader"
{
	Properties
	{
		_Color ("Main Color", Color) = (1, 1, 1, 1)
		_Color2("Accent Color", Color) = (1, 1, 1, 1)
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

			// incoming data from app
			struct appdata
			{
				float4 vertex : POSITION;
				float3 normal : NORMAL;
				float2 lightmapUv : TEXCOORD1; // light data comes in on uv channel 1
			};

			// struct to hold data going from vert stage -> frag stage
			struct v2f
			{
				float2 lightmapUv : TEXCOORD1;
				float4 whichColor : COLOR;
				UNITY_FOG_COORDS(1)
				float4 vertex : SV_POSITION;
			};
			
			// from properties
			float4 _Color;
			float4 _Color2;

			v2f vert (appdata v)
			{
				float3 up = float3(0, 1, 0);
				v2f o;
				o.vertex = UnityObjectToClipPos(v.vertex);
				// select the right part of the lightmap for the object
				o.lightmapUv = v.lightmapUv * unity_LightmapST.xy + unity_LightmapST.zw;
				UNITY_TRANSFER_FOG(o,o.vertex); // do fog...stuff
				// dot product to figure out how slope-ey the surface is
				float d = abs(dot(UnityObjectToWorldNormal(v.normal), up));
				d = d*d*d*d*d; // exaggerate the color difference
				o.whichColor = lerp(_Color, _Color2, 1-d);
				return o;
			}
			
			fixed4 frag (v2f i) : SV_Target
			{
				fixed4 col = i.whichColor;
				// darken areas with lightmap
				col.rgb *= DecodeLightmap(UNITY_SAMPLE_TEX2D(unity_Lightmap, i.lightmapUv));
				// apply fog
				UNITY_APPLY_FOG(i.fogCoord, col);
				return col;
			}
			ENDCG
		}
	}
}
