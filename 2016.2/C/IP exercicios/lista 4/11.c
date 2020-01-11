#include<stdio.h>
#include<stdlib.h>

main()
{
	int A[10], B[10];
	int i,valor;
	
	for(i=0;i<10;i++)
	{
		printf("Digite o valor de A[%i]: ",i);
		scanf("%i", &valor);
		
		A[i]=valor;
	}
	
	for(i=0;i<10;i++)
	{
		if(i%2==0)
		{
			(float) B[i], A[i];
			B[i]=A[i]/2;
		}
		else
		{
			B[i]=A[i]*3;
		}
	}
	
	for(i=0;i<10;i++)
	{
		printf("A[%i]= %i\n\nB[%i]= %i\n\n",i,A[i],i,B[i]);
	}
}
