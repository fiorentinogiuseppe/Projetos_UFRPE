#include<stdio.h>
#include<stdlib.h>
#include<time.h>

main()
{
	float m[10][10];
	int i,j, diagonalprinciapal=0;
	srand(time(NULL));
	
	for(i=0;i<10;i++)
	{
		for(j=0;j<10;j++)
		{
			
			m[i][j]=rand()%10;	
		}
	}
	for(i=0;i<10;i++)
	{
		for(j=0;j<10;j++)
		{
			printf("%.1f\t", m[i][j]);
				
		}
		printf("\n");
	}
	system("pause");
	system("cls");
	
	for(i=0;i<10;i++)
	{
		for(j=0;j<10;j++)
		{
			if(i==j)
			{
				diagonalprinciapal+=m[i][j];
				m[i][j]=diagonalprinciapal;
				printf("%i ", diagonalprinciapal);
			}
			else
			{
				printf(" ");
			}
		}
		printf("\n");
	}
	
	system("pause");
	system("cls");
	printf("\n");
	for(i=1;i<10;i++)
	{
		for(j=0;j<i;j++)
		{
			m[i][j]= m[i][j]*diagonalprinciapal;
		}
		
	}
	for(i=0;i<10;i++)
	{
		for(j=0;j<10;j++)
		{
			printf("%.1f\t", m[i][j]);
		}
		printf("\n");
	}
	
	
}
