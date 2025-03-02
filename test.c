int main()
{
    int a = 1;
    int* p = &a;
    int** p1 = &p;
    int b = 0;
    b = **p1;
    printf("%x\n", &b);
    return 0;
}