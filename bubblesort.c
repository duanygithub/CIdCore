// 冒泡排序函数
void bubbleSort(int *arr, int n) {
    for (int i = 0; i < n - 1; i+=1) {
        // 每轮冒泡将一个最大值"冒"到最后
        for (int j = 0; j < n - i - 1; j+=1) {
            if (arr[j] > arr[j + 1]) {
                // 交换 arr[j] 和 arr[j + 1]
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }
}

// 打印数组函数
void printArray(int *arr, int n) {
    for (int i = 0; i < n; i+=1) {
        printf("%d ", arr[i]);
    }
    printf("\n");
}

// 主函数
int main() {
    int arr[7] = {64, 34, 25, 12, 22, 11, 90};
    int n = 7;

    printf("原始数组: ");
    printArray(arr, n);

    bubbleSort(arr, n);

    printf("排序后数组: ");
    printArray(arr, n);

    return 0;
}
