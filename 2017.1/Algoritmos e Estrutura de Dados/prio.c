#include<stdio.h>
#include<stdlib.h>
#define troca (A, B) { int t = A; A = B; B = t; }

static void Heap (int m, int v[])
{
   int k; 
   for (k = 1; k < m; ++k) {                   
      int f = k+1;
      while (f > 1 && v[f/2] < v[f]) {  
         troca (v[f/2], v[f]);          
         f /= 2;                        
      }
   }
}
