int main()
{
    int num;

    do
    {
        printf("Digite um numero:");
        scanf("%i", &num);
        printf("O triplo desse numero eh: %i\n", num*3);
    }
    while(num!=0);
}
