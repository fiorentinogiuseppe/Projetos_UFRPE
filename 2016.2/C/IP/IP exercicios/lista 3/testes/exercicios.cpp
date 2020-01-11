#include<string.h>
#include<stdio.h>
#include<stdlib.h>
#include <math.h>
#include<time.h>

main()
{
	//Variaveis
	int i, i_2, i_3, j, j_2, j_3, l, l_2, l_3;
	int m[6][6];
	int m_2[6][6];
	int m_3[6][6];

	// primeira matriz
	
	
	for(i=0;i<6; i++)
	{
		
		for(j=0;j<6;j++)
		{
			srand( (unsigned)time(NULL) );

				m[i][j]=rand()*6.0/RAND_MAX;
				printf("%i", m[i][j]);
		
			
		}
		printf("\n");
	}
		printf("\n");
		printf("+");
		printf("\t");
		printf("\n");
		printf("\n");
		
		
	//segunda matriz
	
	for(i_2=0;i_2<6; i_2++)
	{
		for(j_2=0;j_2<6; j_2++)
		{
			srand( (unsigned)time(NULL) );
			l=rand()*6.0/RAND_MAX;

				m_2[i_2][j_2]=rand()*6.0/RAND_MAX;
				printf("%i", m_2[i_2][j_2]);
			
		}
		printf("\n");
	}
		printf("\n");
		printf("=");
		printf("\t");
		printf("\n");
		printf("\n");

	// terceira matriz	
	for(i_3=0;i_3<6; i_3++)
	{
		for(j_3=0;j_3<6; j_3++)
		{
			
				m_3[i_3][j_3]=m_2[i_3][j_3]+ m[i_3][j_3];
				printf("%i", m_3[i_3][j_3]);
			
		}
		printf("\n");
	}
	
}
