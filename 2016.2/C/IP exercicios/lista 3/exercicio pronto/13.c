main()
{
    int valor, tot;
    float media;
    float contagem=0;
    int i=0;
    char resp;

    printf("O total de numeros sera?: ");
    scanf("%i", &tot);

    do
    {
        i++;
        printf("Digite um valor: ");
        scanf("%i", &valor);
        if(valor>=0)
        contagem=contagem+valor;
    }
    while(i<tot);
    media=contagem/tot;
    printf("%.2f", media);



}
