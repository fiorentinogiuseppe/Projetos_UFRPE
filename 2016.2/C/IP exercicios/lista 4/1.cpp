#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<math.h>
#include<time.h>

main()
{
	int tamanho, j, i;
	
	printf("Digite a o tamanho do vetor: ");
	scanf("%i",&tamanho);
	
	int v1[tamanho];
	int v2[tamanho];
	
	for(i=0; i<tamanho; i++)
	{
			printf("Digite o valor %i de v1 e v2: ", i);
			scanf("%i %i", &v1[i], &v2[i]);
	}
	for(i=0; i<tamanho; i++)
	{
		j= v1[i]*v2[i];
		printf("v1[%i] x v2[%i]= %i\n", v1[i], v2[i], j);
	}
}
