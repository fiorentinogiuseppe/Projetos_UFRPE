#include<stdio.h>
#include<stdlib.h>
#include<cstring>

main()
{
	int i;
	char s[81];
	puts("Digite seu nome: ");
	gets(s);
	
	for(i=0;i<strlen(s)+4;i++)
	printf("End=%5u caractere='%c'=%3d\n", &s[i], s[i], s[i]);
	
}
