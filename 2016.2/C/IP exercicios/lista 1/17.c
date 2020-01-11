#include<stdio.h>
#include<stdlib.h>
#include<math.h>
main()
{
	int l1,l2,l3;
	float h, area,x ;
	
	printf("Digite o valor do lado 1: ");
	scanf("%i", &l1);
	printf("Digite o valor do lado 2: ");
	scanf("%i", &l2);
	printf("Digite o valor do lado 3: ");
	scanf("%i", &l3);
	if(l1<l2+l3 && l2<l1+l2 && l3<l1+l2)
	{
		x= pow(l2,2) - pow(l3/2,2);
		printf("%f", x);
		h= sqrt(x);
		printf("%f", h);
		
		area=(l3*h)/2;
		printf(" A area do triangulo eh: %.3f\n", area);
	}
	else{
		printf("Essas medidas nao formam um triangulo");
	}
}
