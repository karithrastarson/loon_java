close all

X1 = importdata('loon/windlayer1_X.txt');
Y1 = importdata('loon/windlayer1_Y.txt');

X2 = importdata('loon/windlayer2_X.txt');
Y2 = importdata('loon/windlayer2_Y.txt');

X3 = importdata('loon/windlayer3_X.txt');
Y3 = importdata('loon/windlayer3_Y.txt');

X4 = importdata('loon/windlayer4_X.txt');
Y4 = importdata('loon/windlayer4_Y.txt');

%coverage = importdata('loon/simulation_coverage2');
figure
subplot(2,2,1)
quiver(X1,Y1)
xlim([1 25])
ylim([1 25])
title('Windlayer 1')
subplot(2,2,2)
quiver(X2,Y2)
xlim([1 25])
ylim([1 25])
title('Windlayer 2')
subplot(2,2,3)
quiver(X3,Y3)
xlim([1 25])
ylim([1 25])
title('Windlayer 3')
subplot(2,2,4)
quiver(X4,Y4)
xlim([1 25])
ylim([1 25])
title('Windlayer 4')
print('report/graphics/WindLayers','-dpdf')

sim_cov_alg1 = importdata('loon/sim_cov_alg1.txt');
figure
plot(sim_cov_alg1(:,1), sim_cov_alg1(:,2))
title('Algorithm 1 - Coverage')
xlabel('t')
ylabel('Coverage')
xlim([0 1000])
ylim([0 1])

%
%fid = fopen('loon/simuluation_coverage.txt','rt');
%a = fscanf(fid, '%e');
%close(fid);

%format short e
%a
%format 