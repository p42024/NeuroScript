import torch
import pandas as pd
from torch import nn
from torch.utils.data import DataLoader, Dataset
from torchvision import datasets
from torchvision.transforms import ToTensor
import os

input = nn.Linear(784, 512)
hidden = nn.Linear(512, 300)
output = nn.Linear(300, 20)
sequential_model = nn.Sequential(input, nn.ReLU(), hidden, nn.Sigmoid(), output)
size = 20

while True:
    if (size == 10):
        break
    else:
        sequential_model = nn.Sequential(sequential_model, nn.ReLU(), nn.Linear(size, (size - 1)))
        size = (size - 1)
torch.save(sequential_model, os.getcwd() + '\\model.pt')

def train_0(sequential_model):
    MNISTDigitsDataset = datasets.MNIST(root="data", train=True, download=True, transform=ToTensor())
    MNISTDigits = DataLoader(MNISTDigitsDataset, batch_size=16)

    MNISTDigitsTestDataset = datasets.MNIST(root="data", train=False, download=True, transform=ToTensor())
    MNISTDigitsTest = DataLoader(MNISTDigitsTestDataset, batch_size=16)

    learning_rate = 0.01
    epoch = 0
    CrossEntropy_0 = nn.CrossEntropyLoss()
    optimiser_0 = torch.optim.SGD(sequential_model.parameters(), lr=learning_rate)
    accuracy_0 = 0
    CrossEntropy_1 = nn.CrossEntropyLoss()

    while True:
        for data, labels in MNISTDigits:
            train_prediction = sequential_model(data)
            loss = CrossEntropy_0(train_prediction, labels)
            loss.backward()
            optimiser_0.step()
            optimiser_0.zero_grad()
        for data, labels in MNISTDigitsTest:
            test_prediction = sequential_model(data)
            accuracy_0 += (test_prediction.argmax(1) == labels).type(torch.float).sum().item()
            loss = CrossEntropy_1(test_prediction, labels)

        accuracy = accuracy_0 / len(MNISTDigitsTest.dataset)
        print("Loss: ")
        print(loss)
        print(", Accuracy: ")
        print(accuracy)
        if ((accuracy > 0.8) or (epoch > 10)):
            break
        epoch = (epoch + 1)

    torch.save(sequential_model, os.getcwd() + '\\trained_model.pt')

class sequential_model_0(nn.Module):
	def __init__(self):
		super().__init__()
		self.flatten = nn.Flatten()
		self.stack = sequential_model

	def forward(self, x):
		if len(x.size()) > 1:
			x = self.flatten(x)
		logits = self.stack(x)
		return logits
train_0(sequential_model_0().to("cpu"))
manual_model = nn.Sequential(nn.Linear(10, 20), nn.ReLU(), nn.Linear(20, 10))

def train_1(manual_model, sequential_model):
    prediction = manual_model(torch.tensor([1.4285715, 1.0, 2.0, 3.0, 4.0, 5.0, 10.0, 4.0, 10.0, 7.0]))
    learning_rate = 0.01
    MSE_0 = nn.MSELoss()
    loss = MSE_0(prediction, torch.tensor([0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0]))
    optimiser_0 = torch.optim.SGD(sequential_model.parameters(), lr=learning_rate)
    loss.backward()
    optimiser_0.step()
    optimiser_0.zero_grad()
    torch.save(sequential_model, os.getcwd() + '\\trained_model.pt')

class sequential_model_1(nn.Module):
	def __init__(self):
		super().__init__()
		self.flatten = nn.Flatten()
		self.stack = sequential_model

	def forward(self, x):
		if len(x.size()) > 1:
			x = self.flatten(x)
		logits = self.stack(x)
		return logits


class manual_model_0(nn.Module):
	def __init__(self):
		super().__init__()
		self.flatten = nn.Flatten()
		self.stack = manual_model

	def forward(self, x):
		if len(x.size()) > 1:
			x = self.flatten(x)
		logits = self.stack(x)
		return logits
train_1(manual_model_0().to("cpu"), sequential_model_1().to("cpu"))
