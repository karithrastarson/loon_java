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

sim_cov_alg1 = importdata('loon/simulation_coverage_alg1_noEdges.txt');
sim_cov_alg2 = importdata('loon/simulation_coverage_alg2_noEdges.txt');
sim_cov_alg3_no_delay = importdata('loon/simulation_coverage_alg3_noDelay.txt');


figure
subplot(2,1,1)
hold on
plot(sim_cov_alg1(:,1), sim_cov_alg1(:,2))
hline = refline([0 0.6]);
hline.Color = 'r';
title('Algorithm 1')
xlabel('t')
ylabel('Coverage')
xlim([0 10000])
ylim([0 1])
subplot(2,1,2)
hold on
plot(sim_cov_alg2(:,1), sim_cov_alg2(:,2))
hline = refline([0 0.6]);
hline.Color = 'r';
title('Algorithm 2')
xlabel('t')
ylabel('Coverage')
xlim([0 10000])
ylim([0 1])
%print('report/graphics/coverage_alg1_vs_alg2','-dpdf')
%

figure
plot(sim_cov_alg3_no_delay(:,1),sim_cov_alg3_no_delay(:,2))